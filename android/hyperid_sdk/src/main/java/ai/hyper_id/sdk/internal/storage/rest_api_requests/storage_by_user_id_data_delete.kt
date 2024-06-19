package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByUserId
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.ResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByUserIdImpl

//**************************************************************************************************
//	StorageByUserIdDataDelete
//--------------------------------------------------------------------------------------------------
internal class StorageByUserIdDataDelete(private val owner		: HyperIDSDKStorageByUserIdImpl,
										val keys				: List<String>,
										val completeListener	: IHyperIDSDKStorageByUserId.IDataDeleteResultListener) : RestApiRequest()
{
	override fun start()
	{
		owner.dataDelete(this)
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
			if(answer.result == 0)			//	SUCCESS
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataDeleteResult.SUCCESS)
			}
			else if(answer.result == -6)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataDeleteResult.FAIL_BY_KEY_ACCESS_DENIED)
			}
			else if(answer.result == -3  || answer.result == -2  || answer.result == -1)
			{
				retry()
			}
			else
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "Service temporary is not available.",
												   null)
			}
		}
	}
}
