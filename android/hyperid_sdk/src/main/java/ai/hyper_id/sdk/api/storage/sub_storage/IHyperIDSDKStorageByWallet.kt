package ai.hyper_id.sdk.api.storage.sub_storage

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.storage.enums.UserDataAccessScope
import ai.hyper_id.sdk.api.storage.enums.UserDataDeleteByWalletResult
import ai.hyper_id.sdk.api.storage.enums.UserDataGetByWalletResult
import ai.hyper_id.sdk.api.storage.enums.UserDataKeysGetByWalletResult
import ai.hyper_id.sdk.api.storage.enums.UserDataSetByWalletResult

interface IHyperIDSDKStorageByWallet
{
	data class WalletInfo(val isPublic	: Boolean,
						  val address	: String,
						  val chainId	: String,
						  val family	: Int,
						  val label		: String,
						  val tags		: List<String>,)
	interface IWalletsGetResultListener
	{
		fun onRequestComplete(result			: IHyperIdSDK.RequestResult,
							  serviceErrorDesc	: String?,
							  wallets			: List<WalletInfo>)
	}

	interface IDataSetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: UserDataSetByWalletResult?)
	}

	data class DataGetResult(val result		: UserDataGetByWalletResult,
							 val dataKey	: String,
							 val dataValue	: String?)
	interface IDataGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: DataGetResult?)
	}

	data class KeysGetResult(val result				: UserDataKeysGetByWalletResult,
							 val keysPublic			: List<String>,
							 val keysPrivate		: List<String>)
	interface IKeysGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: KeysGetResult?)
	}

	data class KeysSharedGetResult(val result		: UserDataKeysGetByWalletResult,
								   val keysShared	: List<String>)
	interface IKeysSharedGetResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  storageResult	: KeysSharedGetResult?)
	}

	interface IDataDeleteResultListener {
		fun onRequestComplete(result		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  serviceResult	: UserDataDeleteByWalletResult?)
	}

	fun walletsGet(completeListener : IWalletsGetResultListener)

	/**
	 * dataSet
	 */
	fun dataSet(walletAddress		: String,
				key					: String,
				value				: String,
				accessType			: UserDataAccessScope,
				completeListener	: IDataSetResultListener)
	/**
	 *
	 */
	fun dataGet(walletAddress		: String,
				key					: String,
				completeListener	: IDataGetResultListener)
	/**
	 *
	 */
	fun keysGet(walletAddress		: String,
				completeListener	: IKeysGetResultListener)
	/**
	 *
	 */
	fun keysSharedGet(walletAddress		: String,
					  completeListener	: IKeysSharedGetResultListener)
	/**
	 *
	 */
	fun dataDelete(walletAddress	: String,
				   keys				: List<String>,
				   completeListener	: IDataDeleteResultListener)

}
