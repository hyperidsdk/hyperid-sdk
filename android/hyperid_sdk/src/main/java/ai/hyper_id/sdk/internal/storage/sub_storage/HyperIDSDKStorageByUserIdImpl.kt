package ai.hyper_id.sdk.internal.storage.sub_storage

import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByUserId
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.storage.UriPaths
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysJson
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysSharedGetJson
import ai.hyper_id.sdk.internal.storage.json.UserDataSetJson
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByUserIdDataDelete
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByUserIdDataGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByUserIdDataSet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByUserIdKeysGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByUserIdKeysSharedGet
import kotlinx.serialization.json.Json

internal class HyperIDSDKStorageByUserIdImpl(_sdkAuth : IRestApiInterface) : HyperIdSDKStorage(_sdkAuth), IHyperIDSDKStorageByUserId
{
	//==================================================================================================
	//	dataSet
	//--------------------------------------------------------------------------------------------------
	override fun dataSet(key				: String,
						 value				: String,
						 accessType			: UserDataAccessScope,
						 completeListener	: IHyperIDSDKStorageByUserId.IDataSetResultListener)
	{
		StorageByUserIdDataSet(this,
							   key,
							   value,
							   accessType,
							   completeListener).start()
	}
	fun dataSet(request : StorageByUserIdDataSet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataSetByUserId,
								   Json.encodeToString(UserDataSetJson.serializer(),
													   UserDataSetJson(request.key,
																	   request.value,
																	   when(request.accessType)
																	   {
																		   UserDataAccessScope.PRIVATE	-> 0
																		   UserDataAccessScope.PUBLIC	-> 1
																	   })),
								   request.requestResultListener)
	}
	//==================================================================================================
	//	dataGet
	//--------------------------------------------------------------------------------------------------
	override fun dataGet(key				: String,
						 completeListener	: IHyperIDSDKStorageByUserId.IDataGetResultListener)
	{
		StorageByUserIdDataGet(this,
							   key,
							   completeListener).start()
	}
	fun dataGet(request : StorageByUserIdDataGet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataGetByUserId,
								   Json.encodeToString(UserDataKeysJson.serializer(),
													   UserDataKeysJson(listOf(request.key))),
								   request.requestResultListener)
	}
	//==================================================================================================
	//	keysGet
	//--------------------------------------------------------------------------------------------------
	override fun keysGet(completeListener : IHyperIDSDKStorageByUserId.IKeysGetResultListener)
	{
		StorageByUserIdKeysGet(this, completeListener).start()
	}
	fun keysGet(request : StorageByUserIdKeysGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetByUserId,
								   emptyList(),
								   request.requestResultListener)
	}
	//==================================================================================================
	//	keysSharedGet
	//--------------------------------------------------------------------------------------------------
	override fun keysSharedGet(completeListener : IHyperIDSDKStorageByUserId.IKeysSharedGetResultListener)
	{
		StorageByUserIdKeysSharedGet(this,
									 completeListener).start()
	}
	fun keysSharedGet(request : StorageByUserIdKeysSharedGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetSharedByUserId,
								   Json.encodeToString(UserDataKeysSharedGetJson.serializer(),
													   UserDataKeysSharedGetJson(request.requestId,
																				 request.nextSearchId,
																				 request.pageSize)),
								   request.requestResultListener)
	}

	//==================================================================================================
	//	dataDelete
	//--------------------------------------------------------------------------------------------------
	override fun dataDelete(keys				: List<String>,
							completeListener	: IHyperIDSDKStorageByUserId.IDataDeleteResultListener)
	{
		StorageByUserIdDataDelete(this,
								  keys,
								  completeListener).start()
	}
	fun dataDelete(request : StorageByUserIdDataDelete)
	{
		restApi.restApiRequestPost(UriPaths.pathDataDeleteByUserId,
								   Json.encodeToString(UserDataKeysJson.serializer(),
													   UserDataKeysJson(request.keys)),
								   request.requestResultListener)
	}
}
