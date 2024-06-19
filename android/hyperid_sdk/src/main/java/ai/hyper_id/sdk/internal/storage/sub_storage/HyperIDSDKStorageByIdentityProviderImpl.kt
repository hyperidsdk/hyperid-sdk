package ai.hyper_id.sdk.internal.storage.sub_storage

import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.storage.UriPaths
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysIdentityProvider
import ai.hyper_id.sdk.internal.storage.json.UserDataKeysSharedGetByIdentityProviderJson
import ai.hyper_id.sdk.internal.storage.json.UserDataSetByIdentityProviderJson
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByIdpDataDelete
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByIdpDataGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByIdpDataSet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByIdpKeysGet
import ai.hyper_id.sdk.internal.storage.rest_api_requests.StorageByIdpKeysSharedGet
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class HyperIDSDKStorageByIdentityProviderImpl(_sdkAuth : IRestApiInterface) : HyperIdSDKStorage(_sdkAuth), IHyperIDSDKStorageByIdentityProvider
{
	//==================================================================================================
	//	dataSet
	//--------------------------------------------------------------------------------------------------
	override fun dataSet(identityProvider	: String,
						 key				: String,
						 value				: String,
						 accessType			: UserDataAccessScope,
						 completeListener	: IHyperIDSDKStorageByIdentityProvider.IDataSetResultListener)
	{
		StorageByIdpDataSet(this,
							identityProvider,
							key,
							value,
							accessType,
							completeListener).start()
	}
	fun dataSet(request : StorageByIdpDataSet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataSetByIdP,
								   Json.encodeToString(UserDataSetByIdentityProviderJson.serializer(),
													   UserDataSetByIdentityProviderJson(request.identityProvider,
																						 request.key,
																						 request.value,
																						 when(request.accessType)
																						 {
																							 UserDataAccessScope.PRIVATE -> 0
																							 UserDataAccessScope.PUBLIC  -> 1
																						 })),
								   request.requestResultListener)
	}


	override fun dataGet(identityProvider	: String,
						 key				: String,
						 completeListener	: IHyperIDSDKStorageByIdentityProvider.IDataGetResultListener)
	{
		StorageByIdpDataGet(this,
							identityProvider,
							key,
							completeListener).start()
	}
	fun dataGet(request : StorageByIdpDataGet)
	{
		restApi.restApiRequestPost(UriPaths.pathDataGetByIdP,
									   Json.encodeToString(UserDataKeysIdentityProvider.serializer(),
														   UserDataKeysIdentityProvider(request.identityProvider,
																						listOf(request.key))),
									   request.requestResultListener)
	}


	override fun keysGet(identityProvider	: String,
						completeListener	: IHyperIDSDKStorageByIdentityProvider.IKeysGetResultListener)
	{
		StorageByIdpKeysGet(this,
							identityProvider,
							completeListener).start()
	}
	fun keysGet(request : StorageByIdpKeysGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysGetByIdP,
								   Json.encodeToString(MapSerializer(String.serializer(), String.serializer()),
													   mapOf(Pair("identity_provider", request.identityProvider))),
								   request.requestResultListener)
	}


	//==================================================================================================
	//	keysSharedGet
	//--------------------------------------------------------------------------------------------------
	override fun keysSharedGet(identityProvider	: String,
							   completeListener : IHyperIDSDKStorageByIdentityProvider.IKeysSharedGetResultListener)
	{
		StorageByIdpKeysSharedGet(this,
								  identityProvider,
								  completeListener).start()
	}
	fun keysSharedGet(request : StorageByIdpKeysSharedGet)
	{
		restApi.restApiRequestPost(UriPaths.pathKeysSharedGetByIdP,
								   Json.encodeToString(UserDataKeysSharedGetByIdentityProviderJson.serializer(),
													   UserDataKeysSharedGetByIdentityProviderJson(request.requestId,
																								   request.identityProvider,
																								   request.nextSearchId,
																								   request.pageSize)),
								   request.requestResultListener)
	}


	override fun dataDelete(identityProvider	: String,
							keys				: List<String>,
							completeListener	: IHyperIDSDKStorageByIdentityProvider.IDataDeleteResultListener)
	{
		StorageByIdpDataDelete(this,
							   identityProvider,
							   keys,
							   completeListener).start()
	}
	fun dataDelete(request : StorageByIdpDataDelete)
	{
		restApi.restApiRequestPost(UriPaths.pathDataDeleteByIdP,
								   Json.encodeToString(UserDataKeysIdentityProvider.serializer(),
													   UserDataKeysIdentityProvider(request.identityProvider,
																					request.keys)),
								   request.requestResultListener)
	}
}
