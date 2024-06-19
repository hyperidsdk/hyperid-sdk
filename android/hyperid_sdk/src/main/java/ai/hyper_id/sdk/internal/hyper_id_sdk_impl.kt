package ai.hyper_id.sdk.internal

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.IHyperIDSDKAuth
import ai.hyper_id.sdk.api.auth.model.ClientInfo
import ai.hyper_id.sdk.api.auth.model.HyperIdProvider
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.api.storage.IHyperIDSDKStorage
import ai.hyper_id.sdk.internal.auth.HyperIDSDKAuthImpl
import ai.hyper_id.sdk.internal.kyc.HyperIDSDKKYCImpl
import ai.hyper_id.sdk.internal.mfa.HyperIdMFAImpl
import ai.hyper_id.sdk.internal.storage.HyperIDSDKStorageImpl

//**************************************************************************************************
//	HyperIdSDKImpl
//--------------------------------------------------------------------------------------------------
class HyperIdSDKImpl : IHyperIdSDK
{
	private val auth									= HyperIDSDKAuthImpl()
	private var kyc 		: IHyperIDSDKKYC?			= null
	private var mfa 		: IHyperIdSDKMFA?			= null
	private var storage 	: IHyperIDSDKStorage?		= null

	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	override fun init(provider			: HyperIdProvider,
					  clientInfo		: ClientInfo,
					  authRestoreInfo	: String?,
					  completeListener	: IHyperIdSDK.IRequestResultListener)
	{
		auth.init(provider,
				  clientInfo,
				  authRestoreInfo,
				  completeListener)
	}
	//==================================================================================================
	//	done
	//--------------------------------------------------------------------------------------------------
	override fun done()
	{
		kyc			= null
		mfa			= null
		storage		= null

		auth.done()
	}

	override fun getAuth()			: IHyperIDSDKAuth			= auth
	override fun getMFA()			: IHyperIdSDKMFA			= mfa			?: HyperIdMFAImpl(auth)
	override fun getKYC()			: IHyperIDSDKKYC			= kyc			?: HyperIDSDKKYCImpl(auth)
	override fun getStorage()		: IHyperIDSDKStorage		= storage		?: HyperIDSDKStorageImpl(auth)
}
