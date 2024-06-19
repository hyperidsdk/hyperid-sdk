import requests
import json
from ..error import (ServerError,
                     AccessTokenExpired,
                     HyperIdAuthenticatorNotInstalled,
                     TransactionNotFound,
                     TransactionAlreadyCompleted)
from .enum import (MfaAvailabilityCheckResult,
                   MfaTransactionStartResult,
                   MfaTransactionStatusGetResult,
                   MfaTransactionCancelResult,
                   MfaTransactionStatus,
                   MfaTransactionCompleteResult)

class Mfa:
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
    
    def check_availability(self, access_token: str) -> bool:
        if not access_token:
            raise AccessTokenExpired
        headers = self.__get_headers(access_token)
        url = self.__build_url("/mfa-client/availability-check")
        try:
            response = requests.post(url, headers=headers, timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = MfaAvailabilityCheckResult(jsn.get('result'))
                if result != MfaAvailabilityCheckResult.SUCCESS:
                    match result:
                        case MfaAvailabilityCheckResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case MfaAvailabilityCheckResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case MfaAvailabilityCheckResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                return bool(jsn.get('is_available'))
            else:
                raise ServerError
        except Exception as e:
            raise

    def start_transaction(self, 
                          access_token : str,
                          code : int,
                          question : str) -> int:
        if not access_token:
            raise AccessTokenExpired
        
        c = str(code)
        if len(c) > 2:
            raise ValueError("The code must be exactly two digits long.")

        if len(c) == 1:
            c = "0" + c
        action = {"type" : "question", "action_info" : question}
        value = {"version": 1, "action": action }
        headers = self.__get_headers(access_token)
        url = self.__build_url("/mfa-client/transaction/start/v2")
        params = {"template_id": 4,
                    "values": json.dumps(value),
                    "code": c
                }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = MfaTransactionStartResult(jsn.get('result'))
                if result != MfaTransactionStartResult.SUCCESS:
                    match result:
                        case MfaTransactionStartResult.FAIL_BY_USER_DEVICE_NOT_FOUND: raise HyperIdAuthenticatorNotInstalled
                        case MfaTransactionStartResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case MfaTransactionStartResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case MfaTransactionStartResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                return int(jsn.get('transaction_id'))
            else:
                raise ServerError
        except Exception as e:
            raise 

    def get_transaction_status(self,
                               access_token : str,
                               transaction_id : int):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/mfa-client/transaction/status-get")
        params = { "transaction_id": transaction_id }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = MfaTransactionStatusGetResult(jsn.get('result'))
                if result != MfaTransactionStatusGetResult.SUCCESS:
                    match result:
                        case MfaTransactionStatusGetResult.FAIL_BY_TRANSACTION_NOT_FOUND: raise TransactionNotFound
                        case MfaTransactionStatusGetResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case MfaTransactionStatusGetResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case MfaTransactionStatusGetResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                transaction_status = MfaTransactionStatus(jsn.get('transaction_status'))
                if transaction_status == MfaTransactionStatus.COMPLETED:
                    complete_result = MfaTransactionCompleteResult(jsn.get('transaction_complete_result'))
                    return transaction_status, complete_result
                return transaction_status
            else:
                raise ServerError
        except Exception as e:
            raise

    def cancel_transaction(self,
                           access_token : str,
                           transaction_id : int):
        if not access_token:
            raise AccessTokenExpired
        
        headers = self.__get_headers(access_token)
        url = self.__build_url("/mfa-client/transaction/cancel")
        params = { "transaction_id": transaction_id }
        try:
            response = requests.post(url, headers=headers, data=json.dumps(params), timeout=self.request_timeout, verify=True)
            if response.status_code in range(200, 299):
                jsn = json.loads(response.text)
                result = MfaTransactionCancelResult(jsn.get('result'))
                if result != MfaTransactionCancelResult.SUCCESS:
                    match result:
                        case MfaTransactionCancelResult.FAIL_BY_ALREADY_CANCELED: return
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_EXPIRED: return
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_COMPLETED: raise TransactionAlreadyCompleted
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_NOT_FOUND: raise TransactionNotFound
                        case MfaTransactionCancelResult.FAIL_BY_TOKEN_INVALID: raise AccessTokenExpired
                        case MfaTransactionCancelResult.FAIL_BY_TOKEN_EXPIRED: raise AccessTokenExpired
                        case MfaTransactionCancelResult.FAIL_BY_ACCESS_DENIED: raise AccessTokenExpired
                        case _: raise ServerError
                return
            else:
                raise ServerError
        except Exception as e:
            raise