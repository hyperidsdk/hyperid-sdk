import requests
import jwt
import datetime
import uuid
import json

from .enum import (InfrastructureType,
                   AuthorizationFlowMode,
                   AuthorizationMethod,
                   WalletGetMode,
                   WalletFamily)
from .auth_token import AuthToken
from .user_info import UserInfo
from .discover import Discover
from .enum import VerificationLevel
from ..error import (HyperIdException,
                     AccessTokenExpired,
                     RefreshTokenExpired,
                     ServerError,
                     UnknownError,
                     WrongCredentialsError)
from ..error_rfc6749 import (OAuth2Error,
                             InvalidRequest,
                             UnauthorizedClient,
                             AccessDenied,
                             UnsupportedResponseType,
                             InvalidScope)
from .client_info import (ClientInfo,
                          ClientInfoBasic,
                          ClientInfoHS256,
                          ClientInfoRSA)

DEFAULT_HEADERS = {
    'Accept': 'application/json',
    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
    'User-Agent': 'HyperID SDK'
}

AUTO_DISCOVER_URI = "/auth/realms/HyperID/.well-known/openid-configuration"

class TransactionData:
    def __init__(self,
                 addressTo : str,
                 chainId : str,
                 addressFrom : str = None,
                 value : str = None,
                 data : str = None,
                 gas : str = None,
                 nonce : str = None):
        self.addressTo = addressTo
        self.chain = chainId
        if(addressFrom) : self.addressFrom = addressFrom
        if(value) : self.value = value
        if(data) : self.data = data
        if(gas) : self.gas = gas
        if(nonce) : self.nonce = nonce

    def isValid(self):
        try:
            int(self.addressTo, 16)
            if(hasattr(self, 'addressFrom')): int(self.addressFrom, 16)
            if(not hasattr(self, 'value') and not hasattr(self, 'data')): return False
            if(hasattr(self, 'value')):
                if(self.value.startswith('0x') or self.value.startswith('0X')):
                    int(self.value, 16)
                else:
                    float(self.value)
            if(hasattr(self, 'data')): int(self.data)
            return True
        except ValueError:
            return False

    def toJson(self):
        data = {
            'to':self.addressTo,
            'chain':self.chain
        }
        if(hasattr(self, 'addressFrom')) : data['from'] = self.addressFrom
        if(hasattr(self, 'value')) : data['value'] = self.value
        if(hasattr(self, 'data')) : data['data'] = self.data
        if(hasattr(self, 'gas')) : data['gas'] = self.gas
        if(hasattr(self, 'nonce')) : data['nonce'] = self.nonce
        return json.dumps(data)

class Auth:
    def __init__(self,
                 client_info : ClientInfo,
                 refresh_token : str = "",
                 infrastructure_type = InfrastructureType.SANDBOX,
                 request_timeout : int = 10):
        self.client_info = client_info
        self.infrastructure_type = infrastructure_type
        self.request_timeout = request_timeout
        self._discover = None
        self._access_token : AuthToken = None
        self._refresh_token : AuthToken = None
        self.transactionResult : str = None
        self.transactionResultDesc : str = None
        self.transactionHash : str = None
        self.__init(refresh_token)

    def __init(self, refresh_token):
        self.__discover_openid_configuration()
        if not self.client_info.is_valid():
            raise WrongCredentialsError
    
        if refresh_token:
            try:
                self.refresh_tokens()
            except:
                raise RefreshTokenExpired

    def __discover_openid_configuration(self):
        discovery_url = self.infrastructure_type.value + AUTO_DISCOVER_URI
        response = requests.get(discovery_url, headers={ 'User-Agent': 'HyperID SDK' })
        if response.status_code == 200:
            self._discover = Discover(response.json())
            print(self._discover.rest_api_token_endpoint)
        else:
            raise ServerError
    
    def __params_prepare(self, payload):
        if self.client_info.auth_method == AuthorizationMethod.BASIC:
            payload['client_id'] = self.client_info.client_id
            payload['client_secret'] = self.client_info.client_secret
        else:
            payload['client_assertion_type'] = 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer'
            payload['client_assertion'] = self.__client_assertion()

    def __client_assertion(self):
        token = {
            'iss': self.client_info.client_id,
            'sub': self.client_info.client_id,
            'aud': self._discover.issuer,
            'exp': self.__datetime_to_epoch(datetime.datetime.utcnow() + datetime.timedelta(minutes=5)), 
            'iat': self.__datetime_to_epoch(datetime.datetime.utcnow()),
            'jti': str(uuid.uuid4()),
            }
        client_assertion = None
        if self.client_info.auth_method == AuthorizationMethod.HS256:
            client_assertion = jwt.encode(token, self.client_info.client_secret, algorithm='HS256')
        if self.client_info.auth_method == AuthorizationMethod.RSA:
            client_assertion = jwt.encode(token, self.client_info.private_key, algorithm='RS256')
        return client_assertion
    
    def __get_authentication_url(self,
                                 flow_mode : AuthorizationFlowMode = AuthorizationFlowMode.SIGN_IN_WEB2,
                                 wallet_get_mode : WalletGetMode = None,
                                 wallet_family : WalletFamily = None,
                                 verification_level : VerificationLevel = None,
                                 identity_provider : str = None) -> str:
        params = {
            "response_type": "code",
            "client_id": self.client_info.client_id,
            "redirect_uri": self.client_info.redirect_uri,
            "scope": self._discover.get_scopes(),
            "flow_mode": flow_mode.value
        }
        if wallet_get_mode:
            params['wallet_get_mode'] = wallet_get_mode.value
        if wallet_family:
            params['wallet_family'] = wallet_family.value
        if verification_level:
            params['verification_level'] = verification_level.value
        if identity_provider:
            params['identity_provider'] = identity_provider
        
        return f"{self._discover.authorization_endpoint}?{'&'.join([f'{k}={v}' for k, v in params.items()])}"

    def start_sign_in_with_transaction(self,
                                       addressTo,
                                       chain,
                                       addressFrom = None,
                                       value = None,
                                       data = None,
                                       gas = None,
                                       nonce = None):
        params = {
            "response_type": "code",
            "client_id": self.client_info.client_id,
            "redirect_uri": self.client_info.redirect_uri,
            "scope": self._discover.get_scopes(),
            "flow_mode": AuthorizationFlowMode.SIGN_IN_WEB2
        }
        transaction = TransactionData(addressTo, chain, addressFrom, value, data, gas, nonce)
        if(not transaction.isValid()): raise HyperIdException("Transaction prameters is invalid")
        params['transaction'] = transaction.toJson()

        return f"{self._discover.authorization_endpoint}?{'&'.join([f'{k}={v}' for k, v in params.items()])}"


    def __datetime_to_epoch(self, dt):
        return int((dt - datetime.datetime(1970, 1, 1)).total_seconds())

    def __handle_oauth_error(self, error_code):
        error_classes = {
            'invalid_request': InvalidRequest,
            'unauthorized_client': UnauthorizedClient,
            'access_denied': AccessDenied,
            'unsupported_response_type': UnsupportedResponseType,
            'invalid_scope': InvalidScope,
        }
    
        error_class = error_classes.get(error_code, OAuth2Error)
        if error_class:
            raise error_class()
        raise ServerError

    def get_discover(self):
        return self._discover
    
    def get_refresh_token(self)-> str:
        if self._refresh_token:
            if not self._refresh_token.is_expired():
                return self._refresh_token.token
            else:
                raise RefreshTokenExpired
        raise RefreshTokenExpired

    def get_access_token(self) -> str:
        if self._access_token:
            if not self._access_token.is_expired():
                return self._access_token.token
            else:
                raise AccessTokenExpired
        raise AccessTokenExpired

    def refresh_tokens(self):
        if not self._refresh_token:
            raise RefreshTokenExpired

        if self._refresh_token.is_expired():
            raise RefreshTokenExpired
    
        payload = {
            "grant_type": "refresh_token",
            "refresh_token": self._refresh_token.token,
        }
        self.__params_prepare(payload)
        response = requests.post(self._discover.token_endpoint, data=payload, headers=DEFAULT_HEADERS, timeout=self.request_timeout, verify=True)
        data = response.json()
        self._access_token = None
        self._refresh_token = None
        if response.status_code in range(200, 299):
            self._access_token = AuthToken(data['access_token'])
            self._refresh_token = AuthToken(data['refresh_token'])
        elif response.status_code in range(400, 499):
            self.__handle_oauth_error(data.get('error'))
        elif response.status_code in range(500, 599):
            raise ServerError
        else:
            raise UnknownError

    def start_sign_in_web2(self, verification_level : VerificationLevel = None):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_WEB2,
                                             verification_level=verification_level)
    
    def start_sign_in_web3(self,
                           verification_level : VerificationLevel = None,
                           wallet_family : WalletFamily = WalletFamily.ETHEREUM):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_WEB3,
                                             verification_level=verification_level,
                                             wallet_family=wallet_family)

    def start_sign_in_guest_upgrade(self):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_GUEST_UPGRADE)

    def start_sign_in_wallet_get(self,
                                 wallet_get_mode : WalletGetMode = WalletGetMode.WALLET_GET_FAST,
                                 wallet_family : WalletFamily = WalletFamily.ETHEREUM):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_WALLET_GET,
                                             wallet_get_mode=wallet_get_mode,
                                             wallet_family=wallet_family)

    def start_sign_in_auto_wallet_get(self):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_WALLET_GET,
                                             wallet_get_mode=WalletGetMode.AUTO_WALLET_GET)

    def start_sign_in_by_identity_provider(self,
                                           identity_provider : str,
                                           verification_level : VerificationLevel = None):
        return self.__get_authentication_url(flow_mode=AuthorizationFlowMode.SIGN_IN_IDENTITY_PROVIDER,
                                             identity_provider=identity_provider,
                                             verification_level=verification_level)

    def exchange_code_to_token(self,
                               authorization_code : str,
                               transaction_result : str = None,
                               transaction_result_desc : str = None,
                               transaction_hash : str = None):
        if(transaction_result) : self.transactionResult = transaction_result
        if(transaction_result_desc) : self.transactionResultDesc = transaction_result_desc
        if(transaction_hash) : self.transactionHash = transaction_hash

        payload = {
            "grant_type": "authorization_code",
            "code": authorization_code,
            "redirect_uri": self.client_info.redirect_uri,
        }
        self.__params_prepare(payload)
        response = requests.post(self._discover.token_endpoint, data=payload, headers=DEFAULT_HEADERS, timeout=self.request_timeout, verify=True)
        data = response.json()
        self._access_token = None
        self._refresh_token = None
        if response.status_code in range(200, 299):
            self._access_token = AuthToken(data['access_token'])
            self._refresh_token = AuthToken(data['refresh_token'])
        elif response.status_code in range(400, 499):
            self.__handle_oauth_error(data.get('error'))
        elif response.status_code in range(500, 599):
            raise ServerError
        else:
            raise UnknownError

    def logout(self):
        if not self._refresh_token:
            self._access_token = None
            self._refresh_token = None
            return

        if self._refresh_token.is_expired():
            self._access_token = None
            self._refresh_token = None
            return

        payload = {
            "refresh_token": self._refresh_token.token,
        }

        self.__params_prepare(payload)
        response = requests.post(self._discover.end_session_endpoint, headers=DEFAULT_HEADERS, data=payload, timeout=self.request_timeout, verify=True)
        self._access_token = None
        self._refresh_token = None
        if response.status_code in range(200, 299):
            pass
        elif response.status_code in range(400, 499):
            self.__handle_oauth_error(response.json().get('error'))
        elif response.status_code in range(500, 599):
            raise ServerError
        else:
            raise UnknownError

    def get_user_info(self) -> UserInfo:
        if self._access_token == None:
            raise AccessTokenExpired

        if self._access_token.is_expired():
            self.refresh_tokens()
        
        return UserInfo(self._access_token.get_decoded_token())
