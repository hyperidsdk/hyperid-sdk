package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysSharedGetResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByEmailImpl

//**************************************************************************************************
//	StorageByEmailKeysSharedGet
//--------------------------------------------------------------------------------------------------
internal class StorageByEmailKeysSharedGet(private val owner	: HyperIDSDKStorageByEmailImpl,
										   val completeListener	: IHyperIDSDKStorageByEmail.IKeysSharedGetResultListener) : RestApiRequest()
{
	private var keys				= mutableListOf<String>()
	var nextSearchId	: String?	= null
	val pageSize					= 100
	val requestId					= idGenerator

	override fun start()
	{
		owner.keysSharedGet(this)
	}
	override fun failWithError(error		: IHyperIdSDK.RequestResult,
							   errorDesc	: String?)
	{
		completeListener.onRequestComplete(error,
										   errorDesc,
										   null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : UserDataKeysSharedGetResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is UserDataKeysSharedGetResultJson)
		{
			if(answer.result == 0)		// SUCCESS
			{
				if(answer.keysShared.isEmpty() || answer.keysShared.size < pageSize)
				{
					keys.addAll(answer.keysShared)
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   IHyperIDSDKStorageByEmail.KeysSharedGetResult(keys))
				}
				else
				{
					nextSearchId = answer.nextSearchId
					start()
				}
			}
			else if(answer.result == 1)		//success_not_found
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByEmail.KeysSharedGetResult(emptyList()))
			}
			else if(answer.result == -3 || answer.result == -2 || answer.result == -1)		//FAIL_BY_ACCESS_DENIED | FAIL_BY_TOKEN_EXPIRED |  FAIL_BY_TOKEN_INVALID
			{
				retry()
			}
			else	// FAIL_BY_INVALID_PARAMETERS | FAIL_BY_SERVICE_TEMPORARY_NOT_VALID
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "Service temporary is not available.",
												   IHyperIDSDKStorageByEmail.KeysSharedGetResult(keys))
			}
		}
	}
}
