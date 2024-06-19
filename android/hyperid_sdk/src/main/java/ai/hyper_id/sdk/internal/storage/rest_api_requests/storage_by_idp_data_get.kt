package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataGetByIdPResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.UserDataGetResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByIdentityProviderImpl

//**************************************************************************************************
//	StorageByIdpDataGet
//--------------------------------------------------------------------------------------------------
internal class StorageByIdpDataGet(private val owner		: HyperIDSDKStorageByIdentityProviderImpl,
								   val identityProvider		: String,
								   val key					: String,
								   val completeListener		: IHyperIDSDKStorageByIdentityProvider.IDataGetResultListener) : RestApiRequest()
{
	override fun start()
	{
		owner.dataGet(this)
	}
	override fun failWithError(error		: IHyperIdSDK.RequestResult,
							   errorDesc	: String?)
	{
		completeListener.onRequestComplete(error,
										   errorDesc,
										   null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : UserDataGetResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is UserDataGetResultJson)
		{
			if(answer.result == 0)		// SUCCESS
			{
				if(answer.values.isNotEmpty())
				{
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   IHyperIDSDKStorageByIdentityProvider.DataGetResult(UserDataGetByIdPResult.SUCCESS,
																										  key,
																										  answer.values[0].value))
				}
				else
				{
					//should be impossible
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   IHyperIDSDKStorageByIdentityProvider.DataGetResult(UserDataGetByIdPResult.SUCCESS,
																										  key,
																										  null))
				}
			}
			else if(answer.result == 1)		//success_not_found
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.DataGetResult(UserDataGetByIdPResult.FAIL_BY_KEY_NOT_FOUND,
																									  key,
																									  null))
			}
			else if(answer.result == -6) // FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.DataGetResult(UserDataGetByIdPResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND,
																									  key,
																									  null))
			}
			else if(answer.result == -7)	//FAIL_BY_KEY_ACCESS_DENIED
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKStorageByIdentityProvider.DataGetResult(UserDataGetByIdPResult.FAIL_BY_KEY_ACCESS_DENIED,
																									  key,
																									  null))
			}
			else if(answer.result == -3 || answer.result == -2 || answer.result == -1) //FAIL_BY_ACCESS_DENIED | FAIL_BY_TOKEN_EXPIRED | FAIL_BY_TOKEN_INVALID
			{
				retry()
			}
			else //FAIL_BY_INVALID_PARAMETERS | FAIL_BY_SERVICE_TEMPORARY_NOT_VALID | FAIL_BY_UNSUPPORTED_ERROR
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   "Service temporary is not available.",
												   null)
			}
		}
	}
}
