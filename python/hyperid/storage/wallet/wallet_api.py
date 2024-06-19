import requests
import json
import hyperid.storage.wallet.result as res
from ...error import (ServerError,
                      DataKeyAccessDenied,
                      AccessTokenExpired,
                      WalletNotFound,
                      DataKeyInvalid)
from ..enum import UserDataAccessScope
from .enum import (UserDataByWalletSetResult,
                   UserDataByWalletGetResult,
                   UserDataKeysByWalletGetResult,
                   UserDataKeysByWalletDeleteResult)

class Wallet:
    def __init__(self,
                 rest_api_base_endpoint,
                 request_timeout : int = 10):
        self.rest_api_base_endpoint = rest_api_base_endpoint
        self.request_timeout = request_timeout

    def __build_url(self, endpoint):
        return self.rest_api_base_endpoint + endpoint
    
    def __get_headers(self, access_token : str):
        headers = {'Content-Type':'application/json',
            'Accept': 'application/json',
            'User-Agent': 'HyperID SDK',
            'Authorization': "Bearer " + access_token}
        return headers
    
    def set_data(self,
                 access_token : str,
                 wallet_address : str,
                 value_key : str,
                 value_data : str,
                 access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-wallet/set")
        params = { "wallet_address": wallet_address,
                    "value_key": value_key,
                    "value_data": value_data,
                    "access_scope": access_scope.value }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataByWalletSetResult(jsn.get('result'))
                if result != UserDataByWalletSetResult.SUCCESS:
                    match result:
                        case UserDataByWalletSetResult.FAIL_BY_KEY_INVALID: raise DataKeyInvalid
                        case UserDataByWalletSetResult.FAIL_BY_KEY_ACCESS_DENIED: raise DataKeyAccessDenied
                        case UserDataByWalletSetResult.FAIL_BY_WALLET_NOT_EXISTS: raise WalletNotFound
                        case UserDataByWalletSetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataByWalletSetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataByWalletSetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise
    
    def get_data(self,
                 access_token : str,
                 wallet_address : str,
                 value_key : str) ->str:
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-wallet/get")
        params = { "wallet_address": wallet_address,
                  "value_keys": [value_key] }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataByWalletGetResult(jsn.get('result'))
                if result != UserDataByWalletGetResult.SUCCESS:
                    match result:
                        case UserDataByWalletGetResult.FAIL_BY_WALLET_NOT_EXISTS: raise WalletNotFound
                        case UserDataByWalletGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataByWalletGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataByWalletGetResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                values = jsn.get("values", [])
                if values:
                    return values[0].get("value_data")
                return None
            else:
                raise ServerError
        except Exception as e:
            raise
    
    def get_keys_list(self,
                      access_token : str,
                      wallet_address : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-wallet/list-get")
        params = { "wallet_address": wallet_address }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByWalletGetResult(jsn.get('result'))
                if result != UserDataKeysByWalletGetResult.SUCCESS:
                    match result:
                        case UserDataKeysByWalletGetResult.FAIL_BY_WALLET_NOT_EXISTS: raise WalletNotFound
                        case UserDataKeysByWalletGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByWalletGetResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                return res.UserDataKeysByWalletGet(jsn)
            else:
                raise ServerError
        except Exception as e:
            raise
    
    def get_keys_list_shared(self,
                             access_token : str,
                             wallet_address : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-wallet/shared-list-get")
        keys_shared = []
        search_id = ""
        try:
            while True:
                params = { "wallet_address": wallet_address,
                          "search_id": search_id,
                          "page_size": 100 }
                response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
                if response.status_code in range(200, 299):
                    jsn = json.loads(response.text)
                    result = UserDataKeysByWalletGetResult(jsn.get('result'))
                    if result != UserDataKeysByWalletGetResult.SUCCESS:
                        match result:
                            case UserDataKeysByWalletGetResult.FAIL_BY_WALLET_NOT_EXISTS: raise WalletNotFound
                            case UserDataKeysByWalletGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                            case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                            case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                            case UserDataKeysByWalletGetResult.SUCCESS_NOT_FOUND: return None
                            case _: raise ServerError
                    ks = jsn.get('keys_shared')
                    search_id = jsn.get('next_search_id')
                    keys_shared.append(ks)
                    if len(ks) < 100:
                        return keys_shared
                else:
                    raise ServerError
        except Exception as e:
            raise

    def delete_data_key(self,
                        access_token : str,
                        wallet_address : str,
                        value_key : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-wallet/delete")
        params = { "wallet_address": wallet_address,
                  "value_keys": [value_key] }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByWalletDeleteResult(jsn.get('result'))
                if result != UserDataKeysByWalletDeleteResult.SUCCESS:
                    match result:
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_WALLET_NOT_EXISTS: raise WalletNotFound
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByWalletDeleteResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise