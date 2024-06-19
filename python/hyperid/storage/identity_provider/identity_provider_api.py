import requests
import json
import hyperid.storage.identity_provider.result as res
from ...error import (ServerError,
                      AccessTokenExpired,
                      IdentityProviderNotFound,
                      DataKeyAccessDenied,
                      DataKeyInvalid)
from ..enum import UserDataAccessScope
from .enum import (UserDataByIdpSetResult,
                   UserDataByIdpGetResult,
                   UserDataKeysByIdpGetResult,
                   UserDataKeysByIdpDeleteResult)

class IdentityProvider:
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
                 identity_provider : str,
                 value_key : str,
                 value_data : str,
                 access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-idp/set")
        params = { "identity_provider": identity_provider,
                    "value_key": value_key,
                    "value_data": value_data,
                    "access_scope": access_scope.value }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataByIdpSetResult(jsn.get('result'))
                if result != UserDataByIdpSetResult.SUCCESS:
                    match result:
                        case UserDataByIdpSetResult.FAIL_BY_KEY_INVALID: raise DataKeyInvalid
                        case UserDataByIdpSetResult.FAIL_BY_KEY_ACCESS_DENIED: raise DataKeyAccessDenied
                        case UserDataByIdpSetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: raise IdentityProviderNotFound
                        case UserDataByIdpSetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataByIdpSetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataByIdpSetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise

    def get_data(self,
                 access_token : str,
                 identity_provider : str,
                 value_key : str) -> str:
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-idp/get")
        params = { "identity_provider": identity_provider,
                  "value_keys": [value_key] }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataByIdpGetResult(jsn.get('result'))
                if result != UserDataByIdpGetResult.SUCCESS:
                    match result:
                        case UserDataByIdpGetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: raise IdentityProviderNotFound
                        case UserDataByIdpGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataByIdpGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataByIdpGetResult.SUCCESS_NOT_FOUND: return None
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
                      identity_provider : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-idp/list-get")
        params = { "identity_provider": identity_provider }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByIdpGetResult(jsn.get('result'))
                if result != UserDataKeysByIdpGetResult.SUCCESS:
                    match result:
                        case UserDataKeysByIdpGetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: raise IdentityProviderNotFound
                        case UserDataKeysByIdpGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByIdpGetResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                return res.UserDataKeysByIDPGet(jsn)
            else:
                raise ServerError
        except Exception as e:
            raise

    def get_keys_list_shared(self,
                             access_token : str,
                             identity_provider : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-idp/shared-list-get")
        keys_shared = []
        search_id=""
        try:
            while True:
                params = { "identity_provider": identity_provider,
                          "search_id": search_id,
                          "page_size": 100 }
                response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
                if response.status_code in range(200, 299):
                    jsn = json.loads(response.text)
                    result = UserDataKeysByIdpGetResult(jsn.get('result'))
                    if result != UserDataKeysByIdpGetResult.SUCCESS:
                        match result:
                            case UserDataKeysByIdpGetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: raise IdentityProviderNotFound
                            case UserDataKeysByIdpGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                            case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                            case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                            case UserDataKeysByIdpGetResult.SUCCESS_NOT_FOUND: return None
                            case _: raise ServerError
                    search_id = jsn.get('next_search_id')
                    ks = jsn.get('keys_shared')
                    keys_shared.append(ks)
                    if len(ks) < 100:
                        return keys_shared
                else:
                    raise ServerError
        except Exception as e:
            raise

    def data_key_delete(self,
                        access_token : str,
                        identity_provider : str,
                        value_key : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-idp/delete")
        params = {"identity_provider": identity_provider,
                  "value_keys": [value_key] }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByIdpDeleteResult(jsn.get('result'))
                if result != UserDataKeysByIdpDeleteResult.SUCCESS:
                    match result:
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_KEY_ACCESS_DENIED: raise DataKeyAccessDenied
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: raise IdentityProviderNotFound
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByIdpDeleteResult.SUCCESS_NOT_FOUND: return
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise