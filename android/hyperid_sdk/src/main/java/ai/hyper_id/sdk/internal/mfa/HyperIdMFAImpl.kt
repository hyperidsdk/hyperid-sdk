package ai.hyper_id.sdk.internal.mfa

import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.mfa.json.TransactionStartRequestParamsJson
import ai.hyper_id.sdk.internal.mfa.rest_api_requests.AvailabilityCheckRequest
import ai.hyper_id.sdk.internal.mfa.rest_api_requests.TransactionCancelRequest
import ai.hyper_id.sdk.internal.mfa.rest_api_requests.TransactionStartRequest
import ai.hyper_id.sdk.internal.mfa.rest_api_requests.TransactionStatusCheckRequest
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

//**************************************************************************************************
//	HyperIdMFAImpl
//--------------------------------------------------------------------------------------------------
internal class HyperIdMFAImpl(private val restApi : IRestApiInterface) : IHyperIdSDKMFA
{
	//==================================================================================================
	//	availabilityCheck
	//--------------------------------------------------------------------------------------------------
	override fun availabilityCheck(completeListener : IHyperIdSDKMFA.IAvailabilityCheckResultListener)
	{
		AvailabilityCheckRequest(this, completeListener).start()
	}
	fun availabilityCheck(request : AvailabilityCheckRequest)
	{
		restApi.restApiRequestPost(UriPaths.AVAILABILITY_CHECK,
								   emptyList(),
								   request.requestResultListener)
	}


	//==================================================================================================
	//	transactionStart
	//--------------------------------------------------------------------------------------------------
	override fun transactionStart(question			: String,
								  code				: String,
								  completeListener	: IHyperIdSDKMFA.ITransactionStartResultListener)
	{
		TransactionStartRequest(this,
								question,
								code,
								completeListener).start()
	}
	fun transactionStart(request : TransactionStartRequest)
	{
		restApi.restApiRequestPost(UriPaths.TRANSACTION_START,
								   Json.encodeToString(TransactionStartRequestParamsJson.serializer(),
													   TransactionStartRequestParamsJson(4,
																						 request.question,
																						 request.code)),
								   request.requestResultListener)
	}


	//==================================================================================================
	//	transactionStatusCheck
	//--------------------------------------------------------------------------------------------------
	override fun transactionStatusCheck(transactionId		: Int,
										completeListener	: IHyperIdSDKMFA.ITransactionStatusCheckResultListener)
	{
		TransactionStatusCheckRequest(this,
									  transactionId,
									  completeListener).start()
	}
	fun transactionStatusCheck(request : TransactionStatusCheckRequest)
	{
		restApi.restApiRequestPost(UriPaths.TRANSACTION_STATUS_GET,
								   Json.encodeToString(MapSerializer(String.serializer(),
																	 Int.serializer()),
													   mapOf("transaction_id"	to request.transactionId)),
								   request.requestResultListener)
	}


	//==================================================================================================
	//	transactionCancel
	//--------------------------------------------------------------------------------------------------
	override fun transactionCancel(transactionId	: Int,
								   completeListener	: IHyperIdSDKMFA.ITransactionCancelResultListener)
	{
		TransactionCancelRequest(this,
								 transactionId,
								 completeListener).start()
	}
	fun transactionCancel(request : TransactionCancelRequest)
	{
		restApi.restApiRequestPost(UriPaths.TRANSACTION_CANCEL,
								   Json.encodeToString(MapSerializer(String.serializer(),
																	 Int.serializer()),
													   mapOf("transaction_id"	to request.transactionId)),
								   request.requestResultListener)
	}

	companion object
	{
		@Suppress("unused")
		private const val TAG	= "HyperIdMFAImpl"
	}
}
