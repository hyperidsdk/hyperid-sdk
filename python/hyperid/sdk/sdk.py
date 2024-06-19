from hyperid.auth.auth import Auth
from hyperid.kyc.kyc_api import Kyc
from hyperid.mfa.mfa_api import Mfa
from hyperid.storage.email.email_api import Email
from hyperid.storage.user_id.user_id_api import UserId
from hyperid.storage.identity_provider.identity_provider_api import IdentityProvider
from hyperid.storage.wallet.wallet_api import Wallet
from hyperid.storage.enum import UserDataAccessScope
from hyperid.auth.enum import (InfrastructureType,
                         VerificationLevel,
                         WalletGetMode,
                         WalletFamily)
from ..error import (AccessTokenExpired,
                     RefreshTokenExpired,
                     AuthorizationRequired,
                     HyperIdException)
from hyperid.auth.user_info import UserInfo
from hyperid.auth.client_info import (ClientInfo,
                                      ClientInfoBasic,
                                      ClientInfoHS256,
                                      ClientInfoRSA)

class Sdk:
    def __init__(self,
                 client_info : ClientInfo,
                 infrastructure_type = InfrastructureType.SANDBOX,
                 request_timeout : int = 10):
        self.request_timeout = request_timeout
        self.auth : Auth = None
        self.kyc : Kyc = None
        self.mfa : Mfa = None
        self.storage_email : Email = None
        self.storage_user_id : UserId = None
        self.storage_idp : IdentityProvider = None
        self.storage_wallet : Wallet = None
        self.__init(client_info, infrastructure_type)
    
    def __init(self, client_info, infrastructure_type):
        self.auth = Auth(client_info=client_info,
                        infrastructure_type=infrastructure_type,
                        request_timeout=self.request_timeout)

        self.kyc = Kyc(self.auth.get_discover().rest_api_token_endpoint)
        self.mfa = Mfa(self.auth.get_discover().rest_api_token_endpoint)
        self.storage_email = Email(self.auth.get_discover().rest_api_token_endpoint)
        self.storage_user_id = UserId(self.auth.get_discover().rest_api_token_endpoint)
        self.storage_idp = IdentityProvider(self.auth.get_discover().rest_api_token_endpoint)
        self.storage_wallet = Wallet(self.auth.get_discover().rest_api_token_endpoint)
        self.initialized = True

    def __get_access_token_wrapped(self):
        access_token = None
        try:
            access_token = self.auth.get_access_token()
            return access_token
        except AccessTokenExpired:
            self.auth.refresh_tokens()
            return self.auth.get_access_token()

    def __function_wrapper(self, func):
        if self.initialized == False:
            raise Exception("Init required")
        try:
            return func()
        except AccessTokenExpired:
            try:
                self.auth.refresh_tokens()
                return func()
            except (AccessTokenExpired, RefreshTokenExpired):
                raise AuthorizationRequired
        except HyperIdException as e:
            raise

    def is_authorized(self) -> bool:
        try:
            return self.auth.get_access_token() != None
        except:
            return False

    def get_discover(self):
        return self.auth.get_discover()
    
    def get_session_restore_info(self) ->str :
        return self.auth.get_refresh_token()

    def start_sign_in_web2(self, verification_level : VerificationLevel = None):
        return self.auth.start_sign_in_web2(verification_level)

    def start_sign_in_web3(self, verification_level : VerificationLevel = None):
        return self.auth.start_sign_in_web3(verification_level)
    
    def start_sign_in_guest_upgrade(self):
        return self.auth.start_sign_in_guest_upgrade()
    
    def start_sign_in_wallet_get(self,
                                 wallet_get_mode = WalletGetMode.WALLET_GET_FAST,
                                 wallet_family : WalletFamily = WalletFamily.ETHEREUM):
        return self.auth.start_sign_in_wallet_get(wallet_get_mode, wallet_family)

    def start_sign_in_by_identity_provider(self, identity_provider : str, verification_level : VerificationLevel = None):
        return self.auth.start_sign_in_by_identity_provider(identity_provider, verification_level)
    
    def complete_sign_in(self, response_url):
        error = response_url.args.get("error")
        if error != None:
            raise Exception(f"Error occured: {error}")
        self.auth.exchange_code_to_token(response_url.args.get("code"))
            
    def sign_out(self):
        self.auth.logout()

    def get_user_info(self) -> UserInfo:
        return self.auth.get_user_info()
        
    def get_user_status(self, verification_level: VerificationLevel = VerificationLevel.KYC_FULL):
        func = lambda: self.kyc.get_user_status(self.__get_access_token_wrapped(), verification_level)
        return self.__function_wrapper(func)

    def get_user_status_top_level(self):
        func = lambda: self.kyc.get_user_status_top_level(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)

    def check_availability(self):
        func = lambda: self.mfa.check_availability(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)
    
    def start_transaction(self, code: int, question : str):
        func = lambda: self.mfa.start_transaction(self.__get_access_token_wrapped(), code, question)
        return self.__function_wrapper(func)
    
    def get_transaction_status(self, transaction_id : int):
        func = lambda: self.mfa.get_transaction_status(self.__get_access_token_wrapped(), transaction_id)
        return self.__function_wrapper(func)
    
    def cancel_transaction(self, transaction_id : int):
        func = lambda: self.mfa.cancel_transaction(self.__get_access_token_wrapped(), transaction_id)
        return self.__function_wrapper(func)
    
    def set_data_by_email(self,
                          value_key : str,
                          value_data : str,
                          access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        func = lambda: self.storage_email.set_data(self.__get_access_token_wrapped(), value_key, value_data, access_scope)
        return self.__function_wrapper(func)

    def get_data_by_email(self, value_key : str):
        func = lambda: self.storage_email.get_data(self.__get_access_token_wrapped(), value_key)
        return self.__function_wrapper(func)

    def get_keys_list_by_email(self):
        func = lambda: self.storage_email.get_keys_list(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)
        
    def get_keys_list_shared_by_email(self):
        func = lambda: self.storage_email.get_keys_list_shared(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)

    def delete_data_key_by_email(self, value_key : str):
        func = lambda: self.storage_email.delete_data_key(self.__get_access_token_wrapped(), value_key)
        return self.__function_wrapper(func)

    def set_data_by_user_id(self,
                          value_key : str,
                          value_data : str,
                          access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        func = lambda: self.storage_user_id.set_data(self.__get_access_token_wrapped(), value_key, value_data, access_scope)
        return self.__function_wrapper(func)

    def get_data_by_user_id(self, value_key : str):
        func = lambda: self.storage_user_id.get_data(self.__get_access_token_wrapped(), value_key)
        return self.__function_wrapper(func)

    def get_keys_list_by_user_id(self):
        func = lambda: self.storage_user_id.get_keys_list(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)

    def get_keys_list_shared_by_user_id(self):
        func = lambda: self.storage_user_id.get_keys_list_shared(self.__get_access_token_wrapped())
        return self.__function_wrapper(func)

    def delete_data_key_by_user_id(self, value_key : str):
        func = lambda: self.storage_user_id.delete_data_key(self.__get_access_token_wrapped(), value_key)
        return self.__function_wrapper(func)

    def set_data_by_identity_provider(self,
                                      identity_provider : str,
                                      value_key : str,
                                      value_data : str,
                                      access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        func = lambda: self.storage_idp.set_data(self.__get_access_token_wrapped(), identity_provider, value_key, value_data, access_scope)
        return self.__function_wrapper(func)

    def get_data_by_identity_provider(self, identity_provider : str, value_key : str):
        func = lambda: self.storage_idp.get_data(self.__get_access_token_wrapped(), identity_provider, value_key)
        return self.__function_wrapper(func)

    def get_keys_list_by_identity_provider(self, identity_provider : str):
        func = lambda: self.storage_idp.get_keys_list(self.__get_access_token_wrapped(), identity_provider)
        return self.__function_wrapper(func)

    def get_keys_list_shared_by_identity_provider(self, identity_provider : str):
        func = lambda: self.storage_idp.get_keys_list_shared(self.__get_access_token_wrapped(), identity_provider)
        return self.__function_wrapper(func)

    def delete_data_key_by_identity_provider(self, identity_provider : str, value_key : str):
        func = lambda: self.storage_idp.data_key_delete(self.__get_access_token_wrapped(), identity_provider, value_key)
        return self.__function_wrapper(func)

    def set_data_by_wallet(self,
                           wallet_address : str,
                           value_key : str,
                           value_data : str,
                           access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        func = lambda: self.storage_wallet.set_data(self.__get_access_token_wrapped(), wallet_address, value_key, value_data, access_scope)
        return self.__function_wrapper(func)

    def get_data_by_wallet(self, wallet_address : str, value_key : str):
        func = lambda: self.storage_wallet.get_data(self.__get_access_token_wrapped(), wallet_address, value_key)
        return self.__function_wrapper(func)

    def get_keys_list_by_wallet(self, wallet_address : str):
        func = lambda: self.storage_wallet.get_keys_list(self.__get_access_token_wrapped(), wallet_address)
        return self.__function_wrapper(func)

    def get_keys_list_shared_by_wallet(self, wallet_address : str):
        func = lambda: self.storage_wallet.get_keys_list_shared(self.__get_access_token_wrapped(), wallet_address)
        return self.__function_wrapper(func)
    
    def delete_data_key_by_wallet(self, wallet_address : str, value_key : str):
        func = lambda: self.storage_wallet.delete_data_key(self.__get_access_token_wrapped(), wallet_address, value_key)
        return self.__function_wrapper(func)