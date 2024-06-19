package ai.hyper_id.sdk.internal.storage.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.storage.json.WalletsGetResultJson
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByWalletImpl

//**************************************************************************************************
//	WalletsGetRequest
//--------------------------------------------------------------------------------------------------
internal class WalletsGetRequest(private val sdkTransaction	: HyperIDSDKStorageByWalletImpl,
								 val completeListener		: IHyperIDSDKStorageByWallet.IWalletsGetResultListener) : RestApiRequest()
{
	override fun start()
	{
		sdkTransaction.walletsGet(this)
	}

	override fun failWithError(error		: IHyperIdSDK.RequestResult,
							   errorDesc	: String?)
	{
		when(error)
		{
			IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED			->
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_INIT_REQUIRED,
												   errorDesc,
												   emptyList())
			IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED	->
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED,
												   errorDesc,
												   emptyList())
			IHyperIdSDK.RequestResult.FAIL_CONNECTION				->
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_CONNECTION,
												   errorDesc,
												   emptyList())
			IHyperIdSDK.RequestResult.FAIL_SERVICE					->
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.FAIL_SERVICE,
												   errorDesc,
												   emptyList())
			IHyperIdSDK.RequestResult.SUCCESS						-> { /* impossible */ }
		}
	}

	override fun parseAnswerOrThrow(jsonString : String) : WalletsGetResultJson
	{
		return jsonParser.decodeFromString(jsonString)
	}

	override fun requestSuccess(answer : Any)
	{
		if(answer is WalletsGetResultJson)
		{
			when(answer.result)
			{
				0				->
				{
					val walletsInfo = mutableListOf<IHyperIDSDKStorageByWallet.WalletInfo>()
					answer.walletsPrivate.forEach()
					{
						walletsInfo.add(IHyperIDSDKStorageByWallet.WalletInfo(false,
																			  it.address,
																			  it.chainId,
																			  it.family,
																			  it.label,
																			  it.tags))
					}
					answer.walletsPublic.forEach()
					{
						walletsInfo.add(IHyperIDSDKStorageByWallet.WalletInfo(true,
																			  it.address,
																			  it.chainId,
																			  it.family,
																			  it.label,
																			  it.tags))
					}
					completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
													   null,
													   walletsInfo)
				}
				-1, -2, -3		->
				{
					retry()
				}
				else			->
				{
					failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
								  "Service temporary is not available.")
				}
			}
		}
	}
}
