package ai.hyper_id.sdk

import ai.hyper_id.sdk.api.auth.IHyperIDSDKAuth
import ai.hyper_id.sdk.api.auth.model.ClientInfo
import ai.hyper_id.sdk.api.auth.model.HyperIdProvider
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.api.storage.IHyperIDSDKStorage
import ai.hyper_id.sdk.internal.HyperIdSDKImpl

fun HyperIdSDKInstance() : IHyperIdSDK = HyperIdSDKImpl()

interface IHyperIdSDK
{
	enum class RequestResult
	{
		FAIL_INIT_REQUIRED,
		FAIL_AUTHORIZATION_REQUIRED,
		FAIL_CONNECTION,
		FAIL_SERVICE,

		SUCCESS,
	}

	interface IRequestResultListener {
		fun onRequestComplete(_result		: RequestResult,
							  _errorDesc	: String?)
	}

	/**
	 * async Init SDK
	 *
	 * @param	provider			Provide full information about service connection endpoint
	 * @param	clientInfo			Provide full information about client application
	 * @param	authRestoreInfo		Optional. Could be provided to avoid authorization request
	 * @param	completeListener	Callback to notify init result
	 **/
	fun init(provider			: HyperIdProvider,
			 clientInfo			: ClientInfo,
			 authRestoreInfo	: String? = null,
			 completeListener	: IRequestResultListener)
	/**
	 * Done SDK
	 **/
	fun done()

	fun getAuth()		: IHyperIDSDKAuth
	fun getMFA()		: IHyperIdSDKMFA
	fun getKYC()		: IHyperIDSDKKYC
	fun getStorage()	: IHyperIDSDKStorage
}
