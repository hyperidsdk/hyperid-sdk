<?php

require_once __DIR__.'/../error.php';
require_once __DIR__.'/../utils.php';

enum KycUserStatus : int {
	case NONE						= 0;
	case PENDING					= 1;
	case COMPLETE_SUCCESS			= 2;
	case COMPLETE_FAIL_RETRYABLE	= 3;
	case COMPLETE_FAIL_FINAL		= 4;
	case DELETED					= 5;
}

enum KycUserStatusGetResult : int {
	case FAIL_BY_USER_KYC_DELETED				= -8;
	case FAIL_BY_USER_NOT_FOUND					= -7;
	case FAIL_BY_BILLING						= -6;
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum KycUserStatusTopLevelGetResult : int {
	case FAIL_BY_INVALID_PARAMETERS				= -7;
	case FAIL_BY_USER_KYC_DELETED				= -6;
	case FAIL_BY_BILLING						= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

class HyperIDKyc {
	public string $restApiEndpoint;

	function __construct(string $restApiEndpoint) {
		$this->restApiEndpoint = $restApiEndpoint;
	}

	function getUserStatus($accessToken,
						   $verificationLevel = VerificationLevel::KYC_BASIC) {
		if(!$accessToken) {
			throw new AuthorizationRequiredException();
		}
		try {
			$params = [
				'verification_level' => $verificationLevel->value
			];
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint."/kyc/user/status-get", $params, $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== KycUserStatusGetResult::SUCCESS->value) {
					switch ($data['result']) {
						case KycUserStatusGetResult::FAIL_BY_USER_KYC_DELETED->value:	return null;
						case KycUserStatusGetResult::FAIL_BY_USER_NOT_FOUND->value:		return null;
						case KycUserStatusGetResult::FAIL_BY_BILLING->value:			return null;
						case KycUserStatusGetResult::FAIL_BY_TOKEN_INVALID->value:		throw new AccessTokenExpiredException();
						case KycUserStatusGetResult::FAIL_BY_TOKEN_EXPIRED->value:		throw new AccessTokenExpiredException();
						case KycUserStatusGetResult::FAIL_BY_ACCESS_DENIED->value:		throw new AccessTokenExpiredException();
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

	function getUserStatusTopLevel($accessToken) {
		if(!$accessToken) {
			throw new Error("Authorization required. Please sign in.");
		}

		try {
			$header[]   = 'Authorization: Bearer '.$accessToken;
			$request = httpPostJSON($this->restApiEndpoint."/kyc/user/status-top-level-get", [], $header);
			if($request['status'] >= 200 && $request['status'] <= 299) {
				$data = $request['response'];
				if($data['result'] !== KycUserStatusTopLevelGetResult::SUCCESS->value) {
					switch ($data['result']) {
						case KycUserStatusTopLevelGetResult::FAIL_BY_USER_KYC_DELETED->value:	return null;
						case KycUserStatusTopLevelGetResult::FAIL_BY_BILLING->value:			return null;
						case KycUserStatusTopLevelGetResult::FAIL_BY_TOKEN_INVALID->value:		throw new AccessTokenExpiredException();
						case KycUserStatusTopLevelGetResult::FAIL_BY_TOKEN_EXPIRED->value:		throw new AccessTokenExpiredException();
						case KycUserStatusTopLevelGetResult::FAIL_BY_ACCESS_DENIED->value:		throw new AccessTokenExpiredException();
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
}

?>