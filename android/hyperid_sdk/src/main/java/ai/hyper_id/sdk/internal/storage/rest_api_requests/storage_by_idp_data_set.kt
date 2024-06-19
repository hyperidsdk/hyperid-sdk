package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByIdPResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.ResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByIdentityProviderImpl

//**************************************************************************************************
//	StorageByIdpDataSet
//--------------------------------------------------------------------------------------------------
internal class StorageByIdpDataSet(private val owner		: HyperIDSDKStorageByIdentityProviderImpl,
								   val identityProvider		: String,
								   val key					: String,
								   val value				: String,
								   val accessType			: UserDataAccessScope,
								   val completeListener		: IHyperIDSDKStorageByIdentityProvider.IDataSetResultListener) : RestApiRequest()
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
			if(answer.result == 0)			//SUCCESS
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataSetByIdPResult.SUCCESS)
			}
			else if(answer.result == -6)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataSetByIdPResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND)
			}
			else if(answer.result == -7)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataSetByIdPResult.FAIL_BY_KEY_ACCESS_DENIED)
			}
			else if(answer.result == -1 || answer.result == -2 || answer.result == -3)		//FAIL_BY_ACCESS_DENIED | FAIL_BY_TOKEN_EXPIRED |  FAIL_BY_TOKEN_INVALID
			{
				retry()
			}
			else //FAIL_BY_INVALID_PARAMETERS | FAIL_BY_SERVICE_TEMPORARY_NOT_VALID
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "Service temporary is not available.",
												   null)
			}
		}
	}
}
