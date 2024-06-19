package ai.hyper_id.sdk.internal.kyc

import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import ai.hyper_id.sdk.internal.kyc.rest_api_requests.RequestUserStatusGet
import ai.hyper_id.sdk.internal.kyc.rest_api_requests.RequestUserStatusTopLevelGet
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

//==================================================================================================
//	HyperIDSDKKYCImpl
//--------------------------------------------------------------------------------------------------
internal class HyperIDSDKKYCImpl(private val restApi : IRestApiInterface) : IHyperIDSDKKYC
{
	private var idGenerator	: Int					= -1
		get()
		{
			return ++field
		}

	//==================================================================================================
	//	getUserStatus
	//--------------------------------------------------------------------------------------------------
	override fun getUserStatus(kycVerificationLevel		: KycVerificationLevel,
							   completeListener			: IHyperIDSDKKYC.IUserStatusGetCompleteListener)
	{
		RequestUserStatusGet(this,
							 idGenerator,
							 kycVerificationLevel,
							 completeListener).start()
	}
	fun getUserStatus(request : RequestUserStatusGet)
	{
		val params = mutableMapOf("request_id"			to	request.requestId,
								  "verification_level"	to	when(request.kycVerificationLevel)
															{
																KycVerificationLevel.BASIC	->  3
																KycVerificationLevel.FULL	->  4
															})

		restApi.restApiRequestPost(UriPaths.STATUS_GET,
								   Json.encodeToString(MapSerializer(String.serializer(),
																	 Int.serializer()),
													   params),
								   request.requestResultListener)
	}
	/**
	 * getUserStatusTopLevel
	 */
	override fun getUserStatusTopLevel(completeListener : IHyperIDSDKKYC.IUserStatusTopLevelGetCompleteListener)
	{
		RequestUserStatusTopLevelGet(this,
									 completeListener).start()
	}
	fun getUserStatusTopLevel(request : RequestUserStatusTopLevelGet)
	{
		restApi.restApiRequestPost(UriPaths.STATUS_TOP_LEVEL_GET,
								   Json.encodeToString(MapSerializer(String.serializer(),
																	 Int.serializer()),
													   mapOf("request_id" to idGenerator)),
								   request.requestResultListener)
	}

	companion object
	{
		@Suppress("unused")
		private const val TAG		= "HyperIDSDKKYCImpl"
	}
}
