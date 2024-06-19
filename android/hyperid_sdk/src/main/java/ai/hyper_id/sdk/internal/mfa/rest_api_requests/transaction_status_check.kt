package ai.hyper_id.sdk.internal.mfa.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.mfa.HyperIdMFAImpl
import ai.hyper_id.sdk.internal.mfa.json.TransactionStatusCheckJson

//**************************************************************************************************
//	TransactionStatusCheckRequest
//--------------------------------------------------------------------------------------------------
internal class TransactionStatusCheckRequest(private val sdkMfa		: HyperIdMFAImpl,
											 val transactionId		: Int,
											 val completeListener	: IHyperIdSDKMFA.ITransactionStatusCheckResultListener) : RestApiRequest()
{
	override fun start()
	{
		sdkMfa.transactionStatusCheck(this)
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
										   transactionId,
										   null,
										   null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : TransactionStatusCheckJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is TransactionStatusCheckJson)
		{
			if(answer.result == 0)
			{
				completeListener.onRequestComplete(IHyperIdSDKMFA.HyperIDMFARequestResult.SUCCESS,
												   null,
												   answer.transactionId,
												   answer.transactionStatus,
												   answer.transactionCompleteResult)
			}
			else if(answer.result == -3
					|| answer.result == -4
					|| answer.result == -5)		//token
			{
				retry()
			}
			else if(answer.result == -6)
			{
				failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_TRANSACTION_NOT_FOUND, null)
			}
			else
			{
				failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_SERVICE,
							  "Service temporary is not available.")
			}
		}
	}
}
