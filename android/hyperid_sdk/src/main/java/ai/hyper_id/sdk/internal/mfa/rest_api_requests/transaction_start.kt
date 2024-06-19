package ai.hyper_id.sdk.internal.mfa.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.mfa.HyperIdMFAImpl
import ai.hyper_id.sdk.internal.mfa.json.TransactionStartResultJson

//**************************************************************************************************
//	TransactionStartRequest
//--------------------------------------------------------------------------------------------------
internal class TransactionStartRequest(private val sdkMfa		: HyperIdMFAImpl,
									   val question				: String,
									   val code					: String,
									   val completeListener		: IHyperIdSDKMFA.ITransactionStartResultListener) : RestApiRequest()
{
	override fun start()
	{
		if(code.length != 2)
		{
			failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_SERVICE,
						  "Parameter @code must contain 2 digits")
		}
		else
		{
			sdkMfa.transactionStart(this)
		}
	}
	override fun failWithError(error : IHyperIdSDK.RequestResult, errorDesc : String?)
	{
		when(error)
		{
			IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED			-> failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_INIT_REQUIRED, errorDesc)
			IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED	-> failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_AUTHORIZATION_REQUIRED, errorDesc)
			IHyperIdSDK.RequestResult.FAIL_CONNECTION				-> failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_CONNECTION, errorDesc)
			IHyperIdSDK.RequestResult.FAIL_SERVICE					-> failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_SERVICE, errorDesc)
			IHyperIdSDK.RequestResult.SUCCESS						-> { /* impossible */ }
		}
	}
	fun failWithError(error		: IHyperIdSDKMFA.HyperIDMFARequestResult,
					  errorDesc	: String?)
	{
		completeListener.onRequestComplete(error,
										   errorDesc,
										   -1)
	}
	override fun parseAnswerOrThrow(jsonString : String) : TransactionStartResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is TransactionStartResultJson)
		{
			if(answer.result == 0)
			{
				completeListener.onRequestComplete(IHyperIdSDKMFA.HyperIDMFARequestResult.SUCCESS,
												   null,
												   answer.transactionId)
			}
			else if(answer.result == -3
					|| answer.result == -4
					|| answer.result == -5)		//token
			{
				retry()
			}
			else if(answer.result == -7)
			{
				failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_MFA_NOT_AVAILABLE,
							  "Failure due to the user's device with HyperID Authenticator App not being found")
			}
			else
			{
				failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_SERVICE,
							  "Service temporary is not available.")
			}
		}
	}
}
