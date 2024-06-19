package ai.hyper_id.sdk.api.storage.sub_storage

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteByIdPResult
import ai.hyper_id.sdk.api.storage.enums.UserDataGetByIdPResult
import ai.hyper_id.sdk.api.storage.enums.UserDataKeysGetByIdPResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByIdPResult

interface IHyperIDSDKStorageByIdentityProvider
{
	interface IDataSetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: UserDataSetByIdPResult?)
	}
	data class DataGetResult(val result 	: UserDataGetByIdPResult,
							 val dataKey	: String,
							 val dataValue	: String?)
	interface IDataGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: DataGetResult?)
	}
	data class KeysGetResult(val result				: UserDataKeysGetByIdPResult,
							 val keysPublic			: List<String>,
							 val keysPrivate		: List<String>)
	interface IKeysGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: KeysGetResult?)
	}
	data class KeysSharedGetResult(val result		: UserDataKeysGetByIdPResult,
								   val keysShared	: List<String>)
	interface IKeysSharedGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: KeysSharedGetResult?)
	}
	interface IDataDeleteResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: UserDataDeleteByIdPResult?)
	}

	fun dataSet(identityProvider	: String,
				key					: String,
				value				: String,
				accessType			: UserDataAccessScope,
				completeListener	: IDataSetResultListener)
	fun dataGet(identityProvider	: String,
				key					: String,
				completeListener	: IDataGetResultListener)
	fun keysGet(identityProvider	: String,
				completeListener	: IKeysGetResultListener)
	fun keysSharedGet(identityProvider	: String,
					  completeListener	: IKeysSharedGetResultListener)
	fun dataDelete(identityProvider	: String,
				   keys				: List<String>,
				   completeListener	: IDataDeleteResultListener)

}
