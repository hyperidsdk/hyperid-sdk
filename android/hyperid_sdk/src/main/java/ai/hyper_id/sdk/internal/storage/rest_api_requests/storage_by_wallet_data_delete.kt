package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteByWalletResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.ResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByWalletImpl

//**************************************************************************************************
//	StorageByWalletDataDelete
//--------------------------------------------------------------------------------------------------
internal class StorageByWalletDataDelete(private val owner		: HyperIDSDKStorageByWalletImpl,
										 val walletAddress		: String,
										 val keys				: List<String>,
										 val completeListener	: IHyperIDSDKStorageByWallet.IDataDeleteResultListener) : RestApiRequest()
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
												   UserDataDeleteByWalletResult.SUCCESS)
			}
			else if(answer.result == -7)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataDeleteByWalletResult.FAIL_BY_KEY_ACCESS_DENIED)
			}
			else if(answer.result == -6)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataDeleteByWalletResult.FAIL_BY_WALLET_NOT_EXISTS)
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
