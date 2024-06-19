import requests
import json
from hyperid.kyc import result as res
from ..error import ServerError, AccessTokenExpired
from ..auth.enum import VerificationLevel
from .enum import KycUserStatusGetResult, KycUserStatusTopLevelGetResult

class Kyc:
    def __init__(self,
                 rest_api_base_endpoint : str,
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
        
    def get_user_status(self,
                        access_token: str,
                        verification_level: VerificationLevel):
        if not access_token:
            raise AccessTokenExpired

        headers = self.__get_headers(access_token)
        url = self.__build_url("/kyc/user/status-get")
        params = { "verification_level": verification_level.value }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = KycUserStatusGetResult(jsn.get('result'))
                if result != KycUserStatusGetResult.SUCCESS:
                    match result:
                        case KycUserStatusGetResult.FAIL_BY_USER_KYC_DELETED: return None
                        case KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND: return None
                        case KycUserStatusGetResult.FAIL_BY_BILLING: return None
                        case KycUserStatusGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case KycUserStatusGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case KycUserStatusGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                return res.KycUserStatusGet(jsn)
            else:
                raise ServerError
        except Exception as e:
            raise

    def get_user_status_top_level(self, access_token: str):
        if not access_token:
            raise AccessTokenExpired

        headers = self.__get_headers(access_token)
        url = self.__build_url("/kyc/user/status-top-level-get")
        try:
            response = requests.post(url, headers=headers, timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = KycUserStatusTopLevelGetResult(jsn.get('result'))
                if result != KycUserStatusTopLevelGetResult.SUCCESS:
                    match result:
                        case KycUserStatusTopLevelGetResult.FAIL_BY_USER_KYC_DELETED: return None
                        case KycUserStatusTopLevelGetResult.FAIL_BY_BILLING: return None
                        case KycUserStatusTopLevelGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case KycUserStatusTopLevelGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case KycUserStatusTopLevelGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                return res.KycUserStatusTopLevelGet(jsn)
            else:
                raise ServerError
        except Exception as e:
            raise