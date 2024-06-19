package ai.hyper_id.sdk.api.storage

import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByUserId
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet

//**************************************************************************************************
//	IHyperIDSDKStorage
//--------------------------------------------------------------------------------------------------
interface IHyperIDSDKStorage
{
	fun StorageByEmail()				: IHyperIDSDKStorageByEmail
	fun StorageByUserId()				: IHyperIDSDKStorageByUserId
	fun StorageByWallet()				: IHyperIDSDKStorageByWallet
	fun StorageByIdentityProvider()		: IHyperIDSDKStorageByIdentityProvider
}
