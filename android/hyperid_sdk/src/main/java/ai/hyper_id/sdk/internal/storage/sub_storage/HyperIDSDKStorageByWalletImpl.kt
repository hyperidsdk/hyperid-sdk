package ai.hyper_id.sdk.internal.storage.sub_storage

import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.storage.UriPaths
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysByWalletJson
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysSharedGetByWalletJson
import ai.hyper_id.sdk.internal.storage.json.UserDataSetByWalletJson
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByWalletDataDelete
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByWalletDataGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByWalletDataSet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByWalletKeysGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByWalletKeysSharedGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.WalletsGetRequest
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class HyperIDSDKStorageByWalletImpl(_sdkAuth : IRestApiInterface) : HyperIdSDKStorage(_sdkAuth), IHyperIDSDKStorageByWallet
{
	/**
	 * getWallets
	 */
	override fun walletsGet(completeListener : IHyperIDSDKStorageByWallet.IWalletsGetResultListener)
	{
		WalletsGetRequest(this, completeListener).start()
	}
	fun walletsGet(request : WalletsGetRequest)
	{
		restApi.restApiRequestPost(UriPaths.pathWalletGet,
								   emptyList(),
								   request.requestResultListener)
	}

	/**
	 * dataSet
	 */
	override fun dataSet(walletAddress		: String,
						 key				: String,
						 value				: String,
						 accessType			: UserDataAccessScope,
						 completeListener	: IHyperIDSDKStorageByWallet.IDataSetResultListener)
	{
		StorageByWalletDataSet(this,
							   walletAddress,
							   key,
							   value,
							   accessType,
							   completeListener).start()
	}
	fun dataSet(request : StorageByWalletDataSet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataSetByWalletId,
								   Json.encodeToString(UserDataSetByWalletJson.serializer(),
													   UserDataSetByWalletJson(request.walletAddress,
																			   request.key,
																			   request.value,
																			   when(request.accessType)
																			   {
																				   UserDataAccessScope.PRIVATE -> 0
																				   UserDataAccessScope.PUBLIC  -> 1
																			   })),
								   request.requestResultListener)
	}

	override fun dataGet(walletAddress		: String,
						 key				: String,
						 completeListener	: IHyperIDSDKStorageByWallet.IDataGetResultListener)
	{
		StorageByWalletDataGet(this,
							   walletAddress,
							   key,
							   completeListener).start()
	}
	fun dataGet(request : StorageByWalletDataGet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataGetByWalletId,
								   Json.encodeToString(UserDataKeysByWalletJson.serializer(),
													   UserDataKeysByWalletJson(request.walletAddress,
																				listOf(request.key))),
								   request.requestResultListener)
	}

	override fun keysGet(walletAddress		: String,
						 completeListener	: IHyperIDSDKStorageByWallet.IKeysGetResultListener)
	{
		StorageByWalletKeysGet(this,
							   walletAddress,
							   completeListener).start()
	}
	fun keysGet(request : StorageByWalletKeysGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetByWalletId,
								   Json.encodeToString(MapSerializer(String.serializer(), String.serializer()),
													   mapOf(Pair("wallet_address", request.walletAddress))),
								   request.requestResultListener)
	}
	//==================================================================================================
	//	keysSharedGet
	//--------------------------------------------------------------------------------------------------
	override fun keysSharedGet(walletAddress	: String,
							   completeListener : IHyperIDSDKStorageByWallet.IKeysSharedGetResultListener)
	{
		StorageByWalletKeysSharedGet(this,
									 walletAddress,
									 completeListener).start()
	}
	fun keysSharedGet(request : StorageByWalletKeysSharedGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysSharedGetByWalletId,
								   Json.encodeToString(UserDataKeysSharedGetByWalletJson.serializer(),
													   UserDataKeysSharedGetByWalletJson(request.requestId,
																						 request.walletAddress,
																						 request.nextSearchId,
																						 request.pageSize)),
								   request.requestResultListener)
	}
	override fun dataDelete(walletAddress		: String,
							keys				: List<String>,
							completeListener	: IHyperIDSDKStorageByWallet.IDataDeleteResultListener)
	{
		StorageByWalletDataDelete(this,
								  walletAddress,
								  keys,
								  completeListener).start()
	}
	fun dataDelete(request : StorageByWalletDataDelete)
	{
		restApi.restApiRequestPost(UriPaths.pathDataDeleteByWalletId,
								   Json.encodeToString(UserDataKeysByWalletJson.serializer(),
													   UserDataKeysByWalletJson(request.walletAddress,
																				request.keys)),
								   request.requestResultListener)
	}
}
