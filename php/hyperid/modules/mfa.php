<?php

require_once __DIR__.'/../error.php';
require_once __DIR__.'/../utils.php';

enum MfaAvailabilityCheckResult : int {
	case FAIL_BY_TOKEN_INVALID					= -5;
	case FAIL_BY_TOKEN_EXPIRED					= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_INVALID_PARAMETERS				= -2;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -1;
	case SUCCESS								= 0;
}

enum MfaTransactionStartResult : int {
	case FAIL_BY_TEMPLATE_NOT_FOUND				= -8;
	case FAIL_BY_USER_DEVICE_NOT_FOUND			= -7;
	case FAIL_BY_TOKEN_INVALID					= -5;
	case FAIL_BY_TOKEN_EXPIRED					= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_INVALID_PARAMETERS				= -2;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -1;
	case SUCCESS								= 0;
}

enum MfaTransactionStatusGetResult : int {
	case FAIL_BY_TRANSACTION_NOT_FOUND			= -6;
	case FAIL_BY_TOKEN_INVALID					= -5;
	case FAIL_BY_TOKEN_EXPIRED					= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_INVALID_PARAMETERS				= -2;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -1;
	case SUCCESS								= 0;
}

enum MfaTransactionCancelResult : int {
	case FAIL_BY_ALREADY_CANCELED				= -10;
	case FAIL_BY_TRANSACTION_COMPLETED			= -9;
	case FAIL_BY_TRANSACTION_EXPIRED			= -8;
	case FAIL_BY_TRANSACTION_NOT_FOUND			= -6;
	case FAIL_BY_TOKEN_INVALID					= -5;
	case FAIL_BY_TOKEN_EXPIRED					= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_INVALID_PARAMETERS				= -2;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -1;
	case SUCCESS								= 0;
}

enum MfaTransactionStatus : int {
	case PENDING	= 0;
	case COMPLETED	= 1;
	case EXPIRED	= 2;
	case CANCELED	= 4;
}

enum MfaTransactionCompleteResult : int {
	case APPROVED	= 0;
	case DENIED		= 1;
}

class HyperIDMfa {
	public string $restApiEndpoint;

	function __construct(string $restApiEndpoint) {
		$this->restApiEndpoint = $restApiEndpoint;
	}

	function checkAvailability($accessToken) {
		if(!$accessToken) {
			throw new AuthorizationRequiredException();
		}
		try {
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint.'/mfa-client/availability-check', [], $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== MfaAvailabilityCheckResult::SUCCESS->value) {
					switch ($data['result']) {
						case MfaAvailabilityCheckResult::FAIL_BY_TOKEN_INVALID->value:		throw new AccessTokenExpiredException();
						case MfaAvailabilityCheckResult::FAIL_BY_TOKEN_EXPIRED->value:		throw new AccessTokenExpiredException();
						case MfaAvailabilityCheckResult::FAIL_BY_ACCESS_DENIED->value:		throw new AccessTokenExpiredException();
						default : throw new ServerErrorException();
					}
				}
				return $data['is_available'];
			} else {
				throw new ServerErrorException();
			}
		} catch (Exception $error) {
			throw $error;
		}
	}

	function startTransaction($accessToken, string $code, string $question) {
		if(!$accessToken) {
			throw new AuthorizationRequiredException();
		}

		if (strlen($code) > 2) {
			throw new HyperIdException('The code must be exactly two digits long.');
		}
		if (strlen($code) === 1) {
			$code = "0" . $code;
		}

		$action = [ 'type' => "question", 'action_info' => $question ];
		$value = [ 'version' => 1, 'action' => $action ];
		$params = [
			'template_id' => 4,
			'values' => $value,
			'code' => $code,
		];

		try {
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint.'/mfa-client/transaction/start/v2', $params, $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== MfaTransactionStartResult::SUCCESS->value) {
					switch ($data['result']) {
						case MfaTransactionStartResult::FAIL_BY_USER_DEVICE_NOT_FOUND->value:	throw new HyperIdException('HyperId Authenticator not install, please install it first.');
						case MfaTransactionStartResult::FAIL_BY_TOKEN_INVALID->value:			throw new AccessTokenExpiredException();
						case MfaTransactionStartResult::FAIL_BY_TOKEN_EXPIRED->value:			throw new AccessTokenExpiredException();
						case MfaTransactionStartResult::FAIL_BY_ACCESS_DENIED->value:			throw new AccessTokenExpiredException();
						default : throw new ServerErrorException();
					}
				}
				return $data['transaction_id'];
			} else {
				throw new ServerErrorException();
			}
		} catch (Exception $error) {
			throw $error;
		}
	}

	function getTransactionStatus($accessToken, string $transactionId) {
		if(!$accessToken) {
			throw new AuthorizationRequiredException();
		}

		$params = [
			'transaction_id' => $transactionId
		];

		try {
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint.'/mfa-client/transaction/status-get', $params, $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== MfaTransactionStatusGetResult::SUCCESS->value) {
					switch ($data['result']) {
						case MfaTransactionStatusGetResult::FAIL_BY_TRANSACTION_NOT_FOUND->value:	throw new HyperIdException('Transaction not found.');
						case MfaTransactionStatusGetResult::FAIL_BY_TOKEN_INVALID->value:			throw new AccessTokenExpiredException();
						case MfaTransactionStatusGetResult::FAIL_BY_TOKEN_EXPIRED->value:			throw new AccessTokenExpiredException();
						case MfaTransactionStatusGetResult::FAIL_BY_ACCESS_DENIED->value:			throw new AccessTokenExpiredException();
						default : throw new ServerErrorException();
					}
				}
				return $data;
			} else {
				throw new ServerErrorException();
			}
		} catch (Exception $error) {
			throw $error;
		}
	}

	function cancelTransaction($accessToken, string $transactionId) {
		if(!$accessToken) {
			throw new AuthorizationRequiredException();
		}

		$params = [
			'transaction_id' => $transactionId
		];

		try {
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint.'/mfa-client/transaction/cancel', $params, $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== MfaTransactionCancelResult::SUCCESS->value) {
					switch ($data['result']) {
						case MfaTransactionCancelResult::FAIL_BY_ALREADY_CANCELED->value:		return;
						case MfaTransactionCancelResult::FAIL_BY_TRANSACTION_EXPIRED->value:	return;
						case MfaTransactionCancelResult::FAIL_BY_TRANSACTION_COMPLETED->value:	throw new HyperIdException('Transaction already completed.');
						case MfaTransactionCancelResult::FAIL_BY_TRANSACTION_NOT_FOUND->value:	throw new HyperIdException('Transaction not found.');
						case MfaTransactionCancelResult::FAIL_BY_TOKEN_INVALID->value:			throw new AccessTokenExpiredException();
						case MfaTransactionCancelResult::FAIL_BY_TOKEN_EXPIRED->value:			throw new AccessTokenExpiredException();
						case MfaTransactionCancelResult::FAIL_BY_ACCESS_DENIED->value:			throw new AccessTokenExpiredException();
						default : throw new ServerErrorException();
					}
				}
				return;
			} else {
				throw new ServerErrorException();
			}
		} catch (Exception $error) {
			throw $error;
		}
	}
}

?>