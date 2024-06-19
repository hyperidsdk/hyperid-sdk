package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataKeysGetByIdPResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysGetResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByIdentityProviderImpl

//**************************************************************************************************
//	StorageByIdpKeysGet
//--------------------------------------------------------------------------------------------------
internal class StorageByIdpKeysGet(private val owner		: HyperIDSDKStorageByIdentityProviderImpl,
								   val identityProvider		: String,
								   val completeListener		: IHyperIDSDKStorageByIdentityProvider.IKeysGetResultListener) : RestApiRequest()
{
	override fun start()
	{
		owner.keysGet(this)
	}
	override fun failWithError(error		: IHyperIdSDK.RequestResult,
							   errorDesc	: String?)
	{
		completeListener.onRequestComplete(error,
										   errorDesc,
										   null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : UserDataKeysGetResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is UserDataKeysGetResultJson)
		{
			if(answer.result == 0)		// SUCCESS
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.KeysGetResult(UserDataKeysGetByIdPResult.SUCCESS,
																									  answer.keysPublic,
																									  answer.keysPrivate))
			}
			else if(answer.result == 1)		// SUCCESS
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.KeysGetResult(UserDataKeysGetByIdPResult.SUCCESS,
																									  emptyList(),
																									  emptyList()))
			}
			else if(answer.result == -6)		//FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.KeysGetResult(UserDataKeysGetByIdPResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND,
																									  emptyList(),
																									  emptyList()))
			}
			else if(answer.result == -3 || answer.result == -2 || answer.result == -1)		//FAIL_BY_ACCESS_DENIED | FAIL_BY_TOKEN_EXPIRED |  FAIL_BY_TOKEN_INVALID
			{
				retry()
			}
			else	// FAIL_BY_INVALID_PARAMETERS | FAIL_BY_SERVICE_TEMPORARY_NOT_VALID
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "Service temporary is not available.",
												   null)
			}
		}
	}
}
