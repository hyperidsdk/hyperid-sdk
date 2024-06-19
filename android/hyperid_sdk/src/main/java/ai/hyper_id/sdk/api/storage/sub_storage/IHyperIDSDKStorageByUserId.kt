package ai.hyper_id.sdk.api.storage.sub_storage

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteResult
import ai.hyper_id.sdk.api.storage.enums.UserDataGetResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetResult

interface IHyperIDSDKStorageByUserId
{
	interface IDataSetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: UserDataSetResult?)
	}


	data class DataGetResult(val result 	: UserDataGetResult,
							 val key		: String?,
							 val value		: String?)
	interface IDataGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: DataGetResult?)
	}


	data class KeysGetResult(val keysPublic			: List<String>,
							 val keysPrivate		: List<String>)
	interface IKeysGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: KeysGetResult?)
	}

	data class KeysSharedGetResult(val keysShared	: List<String>)
	interface IKeysSharedGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: KeysSharedGetResult?)
	}

	interface IDataDeleteResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: UserDataDeleteResult?)
	}
	/**
	 *
	 */
	fun dataSet(key					: String,
				value				: String,
				accessType			: UserDataAccessScope,
				completeListener	: IDataSetResultListener)
	/**
	 *
	 */
	fun dataGet(key					: String,
				completeListener	: IDataGetResultListener)
	/**
	 *
	 */
	fun keysGet(completeListener	: IKeysGetResultListener)
	/**
	 *
	 */
	fun keysSharedGet(completeListener	: IKeysSharedGetResultListener)
	/**
	 *
	 */
	fun dataDelete(keys				: List<String>,
				   completeListener	: IDataDeleteResultListener)
}
