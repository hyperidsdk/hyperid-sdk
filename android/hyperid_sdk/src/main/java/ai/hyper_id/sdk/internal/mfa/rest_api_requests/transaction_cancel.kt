package ai.hyper_id.sdk.internal.mfa.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.mfa.HyperIdMFAImpl
import ai.hyper_id.sdk.internal.mfa.json.TransactionCancelJson

//**************************************************************************************************
//	TransactionCancelRequest
//--------------------------------------------------------------------------------------------------
internal class TransactionCancelRequest(private val sdkMfa		: HyperIdMFAImpl,
										val transactionId		: Int,
										val completeListener	: IHyperIdSDKMFA.ITransactionCancelResultListener) : RestApiRequest()
{
	override fun start()
	{
		sdkMfa.transactionCancel(this)
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
										   transactionId)
	}
	override fun parseAnswerOrThrow(jsonString : String) : TransactionCancelJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is TransactionCancelJson)
		{
			if(answer.result == 0			//success
			   || answer.result == -6		//Failure due to a transaction with the provided ID is not found
			   || answer.result == -8		//Failure due to a transaction with the provided ID has already been expired
			   || answer.result == -9		//Failure due to a transaction with the provided ID has already been completed
			   || answer.result == -10)		//Failure due to a transaction with the provided ID has already been canceled
			{
				completeListener.onRequestComplete(IHyperIdSDKMFA.HyperIDMFARequestResult.SUCCESS,
												   null,
												   transactionId)
			}
			else if(answer.result == -3
					|| answer.result == -4
					|| answer.result == -5)		//token
			{
				retry()
			}
			else
			{
				failWithError(IHyperIdSDKMFA.HyperIDMFARequestResult.FAIL_SERVICE,
							  "Service temporary is not available.")
			}
		}
	}
}
