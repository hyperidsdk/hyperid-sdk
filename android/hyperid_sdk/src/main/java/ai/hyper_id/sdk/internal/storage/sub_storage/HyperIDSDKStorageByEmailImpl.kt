package ai.hyper_id.sdk.internal.storage.sub_storage

import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.storage.UriPaths
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysJson
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysSharedGetJson
import ai.hyper_id.sdk.internal.storage.json.UserDataSetJson
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByEmailDataDelete
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByEmailDataGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByEmailDataSet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByEmailKeysGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByEmailKeysSharedGet
import kotlinx.serialization.json.Json

internal class HyperIDSDKStorageByEmailImpl(_sdkAuth : IRestApiInterface) : HyperIdSDKStorage(_sdkAuth), IHyperIDSDKStorageByEmail
{
	//==================================================================================================
	//	dataSet
	//--------------------------------------------------------------------------------------------------
	override fun dataSet(key				: String,
						 value				: String,
						 accessType			: UserDataAccessScope,
						 completeListener	: IHyperIDSDKStorageByEmail.IDataSetResultListener)
	{
		StorageByEmailDataSet(this,
							  key,
							  value,
							  accessType,
							  completeListener).start()
	}
	fun dataSet(request : StorageByEmailDataSet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataSetByEmail,
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
						 completeListener	: IHyperIDSDKStorageByEmail.IDataGetResultListener)
	{
		StorageByEmailDataGet(this,
							  key,
							  completeListener).start()
	}
	fun dataGet(request : StorageByEmailDataGet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataGetByEmail,
								   Json.encodeToString(UserDataKeysJson.serializer(),
													   UserDataKeysJson(listOf(request.key))),
								   request.requestResultListener)
	}

	//==================================================================================================
	//	keysGet
	//--------------------------------------------------------------------------------------------------
	override fun keysGet(completeListener : IHyperIDSDKStorageByEmail.IKeysGetResultListener)
	{
		StorageByEmailKeysGet(this, completeListener).start()
	}
	fun keysGet(request : StorageByEmailKeysGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetByEmail,
								   emptyList(),
								   request.requestResultListener)
	}

	//==================================================================================================
	//	keysSharedGet
	//--------------------------------------------------------------------------------------------------
	override fun keysSharedGet(completeListener : IHyperIDSDKStorageByEmail.IKeysSharedGetResultListener)
	{
		StorageByEmailKeysSharedGet(this,
									completeListener).start()
	}
	fun keysSharedGet(request : StorageByEmailKeysSharedGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetSharedByEmail,
									Json.encodeToString(UserDataKeysSharedGetJson.serializer(),
														UserDataKeysSharedGetJson(request.requestId,
																				  request.nextSearchId,
																				  request.pageSize)),
									request.requestResultListener)
	}

	//==================================================================================================
	//	dataDelete
	//--------------------------------------------------------------------------------------------------
	override fun dataDelete(keys : List<String>,
							completeListener : IHyperIDSDKStorageByEmail.IDataDeleteResultListener)
	{
		StorageByEmailDataDelete(this,
								 keys,
								 completeListener).start()
	}
	fun dataDelete(request : StorageByEmailDataDelete)
	{
		restApi.restApiRequestPost(UriPaths.pathDataDeleteByEmail,
								   Json.encodeToString(UserDataKeysJson.serializer(),
													   UserDataKeysJson(request.keys)),
								   request.requestResultListener)
	}
}
