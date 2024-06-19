package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByWalletResult
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.ResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByWalletImpl

//**************************************************************************************************
//	StorageByWalletDataSet
//--------------------------------------------------------------------------------------------------
internal class StorageByWalletDataSet(private val owner		: HyperIDSDKStorageByWalletImpl,
									  val walletAddress		: String,
									  val key				: String,
									  val value				: String,
									  val accessType		: UserDataAccessScope,
									  val completeListener	: IHyperIDSDKStorageByWallet.IDataSetResultListener) : RestApiRequest()
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
												   UserDataSetByWalletResult.SUCCESS)
			}
			else if(answer.result == -6)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataSetByWalletResult.FAIL_BY_WALLET_NOT_EXISTS)
			}
			else if(answer.result == -7)
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   UserDataSetByWalletResult.FAIL_BY_KEY_ACCESS_DENIED)
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
