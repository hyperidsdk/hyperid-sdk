package ai.hyper_id.sdk.internal.storage

import ai.hyper_id.sdk.api.storage.IHyperIDSDKStorage
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByEmail
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByIdentityProvider
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByUserId
import ai.hyper_id.sdk.api.storage.sub_storage.IHyperIDSDKStorageByWallet
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByEmailImpl
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByIdentityProviderImpl
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByUserIdImpl
import ai.hyper_id.sdk.internal.storage.sub_storage.HyperIDSDKStorageByWalletImpl

//**************************************************************************************************
//	IHyperIDSDKStorage
//--------------------------------------------------------------------------------------------------
internal class HyperIDSDKStorageImpl(private val restApi : IRestApiInterface) : IHyperIDSDKStorage
{
	override fun StorageByEmail()				: IHyperIDSDKStorageByEmail				= HyperIDSDKStorageByEmailImpl(restApi)
	override fun StorageByUserId()				: IHyperIDSDKStorageByUserId			= HyperIDSDKStorageByUserIdImpl(restApi)
	override fun StorageByWallet()				: IHyperIDSDKStorageByWallet			= HyperIDSDKStorageByWalletImpl(restApi)
	override fun StorageByIdentityProvider()	: IHyperIDSDKStorageByIdentityProvider	= HyperIDSDKStorageByIdentityProviderImpl(restApi)
}
