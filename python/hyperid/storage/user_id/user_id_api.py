import requests
import json
import hyperid.storage.user_id.result as res
from ...error import (ServerError,
                      AccessTokenExpired,
                      DataKeyAccessDenied,
                      DataKeyInvalid)
from ..enum import UserDataAccessScope
from .enum import (UserDataSetByUserIdResult,
                   UserDataGetByUserIdResult,
                   UserDataKeysByUserIdGetResult,
                   UserDataKeysByUserIdDeleteResult)

class UserId:
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
                 value_key : str,
                 value_data : str,
                 access_scope: UserDataAccessScope = UserDataAccessScope.PUBLIC):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-user-id/set")
        params = {"value_key": value_key,
                "value_data": value_data,
                "access_scope": access_scope.value }
        response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
        if response.status_code in range(200, 299):
            try:
                jsn = json.loads(response.text)
                result = UserDataSetByUserIdResult(jsn.get('result'))
                if result != UserDataSetByUserIdResult.SUCCESS:
                    match result:
                        case UserDataSetByUserIdResult.FAIL_BY_KEY_INVALID: raise DataKeyInvalid
                        case UserDataSetByUserIdResult.FAIL_BY_KEY_ACCESS_DENIED: raise DataKeyAccessDenied
                        case UserDataSetByUserIdResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataSetByUserIdResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataSetByUserIdResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case _: raise ServerError
                return
            except:
                raise ServerError
        raise ServerError

    def get_data(self,
                 access_token : str,
                 value_key : str) ->str:
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-user-id/get")
        params = {"value_keys": [value_key]}
        response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
        if response.status_code in range(200, 299):
            try:
                jsn = json.loads(response.text)
                result = UserDataGetByUserIdResult(jsn.get('result'))
                if result != UserDataGetByUserIdResult.SUCCESS:
                    match result:
                        case UserDataGetByUserIdResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataGetByUserIdResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataGetByUserIdResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataGetByUserIdResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                values = jsn.get("values", [])
                if values:
                    return values[0].get("value_data")
                return None
            except:
                raise ServerError
        raise ServerError
    
    def get_keys_list(self,
                      access_token : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-user-id/list-get")
        try:
            response = requests.post(url, headers=headers,  timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByUserIdGetResult(jsn.get('result'))
                if result != UserDataKeysByUserIdGetResult.SUCCESS:
                    match result:
                        case UserDataKeysByUserIdGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByUserIdGetResult.SUCCESS_NOT_FOUND: return None
                        case _: raise ServerError
                return res.UserDataKeysByUserIdGet(jsn)    
            else:
                raise ServerError
        except Exception as e:
            raise

    def get_keys_list_shared(self,
                             access_token : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-user-id/shared-list-get")
        keys_shared = []
        search_id = ""
        try:
            while True:
                params = { "search_id": search_id,
                          "page_size": 100 }
                response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
                if response.status_code in range(200, 299):
                    jsn = json.loads(response.text)
                    result = UserDataKeysByUserIdGetResult(jsn.get('result'))
                    if result != UserDataKeysByUserIdGetResult.SUCCESS:
                        match result:
                            case UserDataKeysByUserIdGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                            case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                            case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                            case UserDataKeysByUserIdGetResult.SUCCESS_NOT_FOUND: return None
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
                        value_key : str):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/user-data/by-user-id/delete")
        params = { "value_keys": [value_key] }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = UserDataKeysByUserIdDeleteResult(jsn.get('result'))
                if result != UserDataKeysByUserIdDeleteResult.SUCCESS:
                    match result:
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_KEY_ACCESS_DENIED: raise DataKeyAccessDenied
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case UserDataKeysByUserIdDeleteResult.SUCCESS_NOT_FOUND: return
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise