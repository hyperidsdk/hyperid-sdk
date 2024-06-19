package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataSetResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.mfa.HyperIdMFAImpl
import ai.hyper_id.sdk.internal.mfa.json.AvailabilityCheckResultJson
import ai.hyper_id.sdk.internal.storage.json.ResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByEmailImpl

//**************************************************************************************************
//	StorageByEmailDataSet
//--------------------------------------------------------------------------------------------------
internal class StorageByEmailDataSet(private val owner		: HyperIDSDKStorageByEmailImpl,
									 val key				: String,
									 val value				: String,
									 val accessType			: UserDataAccessScope,
									 val completeListener	: IHyperIDSDKStorageByEmail.IDataSetResultListener) : RestApiRequest()
{
	override fun start()
	{
		owner.dataSet(this)
	}
	override fun failWithError(error		: IHyperIdSDK.RequestResult,
							   errorDesc	: String?)
	{
		completeListener.onRequestComplete(error,
										   errorDesc,
										   null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : ResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is ResultJson)
		{
			when(answer.result)
			{
				0		->		//	SUCCESS
				{
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   UserDataSetResult.SUCCESS)
				}
				-6		->
				{
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   UserDataSetResult.FAIL_BY_KEY_ACCESS_DENIED)
				}
				-3,				// FAIL_BY_ACCESS_DENIED
				-2,				// FAIL_BY_TOKEN_EXPIRED
				-1		->		// FAIL_BY_TOKEN_INVALID
				{
					retry()
				}
				else	->
				{
					//-4  FAIL_BY_SERVICE_TEMPORARY_NOT_VALID
					//-5  FAIL_BY_INVALID_PARAMETERS
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
													   "Service temporary is not available.",
													   null)
				}
			}
		}
	}
}
