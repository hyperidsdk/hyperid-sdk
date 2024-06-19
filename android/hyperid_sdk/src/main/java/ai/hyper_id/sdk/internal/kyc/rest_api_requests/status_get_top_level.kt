package ai.hyper_id.sdk.internal.kyc.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatus
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatusTopLevelGetResult
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.kyc.HyperIDSDKKYCImpl
import ai.hyper_id.sdk.internal.kyc.json.StatusTopLevelGetResponseJson

internal class RequestUserStatusTopLevelGet(private val sdkKyc		: HyperIDSDKKYCImpl,
											val completeListener	: IHyperIDSDKKYC.IUserStatusTopLevelGetCompleteListener) : RestApiRequest()
{
	override fun start()
	{
		sdkKyc.getUserStatusTopLevel(this)
	}
	override fun failWithError(error : IHyperIdSDK.RequestResult, errorDesc : String?)
	{
		completeListener.onRequestComplete(error, errorDesc, null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : StatusTopLevelGetResponseJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is StatusTopLevelGetResponseJson)
		{
			val verificationLevel =	if(answer.verificationLevelRaw == 3)
									{
										KycVerificationLevel.BASIC
									}
									else
									{
										KycVerificationLevel.FULL
									}
			val userStatus 		=	when(answer.userStatus)
									{
										0		-> KycUserStatus.NONE
										1		-> KycUserStatus.PENDING
										2		-> KycUserStatus.COMPLETE_SUCCESS
										3		-> KycUserStatus.COMPLETE_FAIL_RETRAYABLE
										4		-> KycUserStatus.COMPLETE_FAIL_FINAL
										5		-> KycUserStatus.DELETED
										else	-> KycUserStatus.NONE
									}
			if (answer.resultRaw == 0			// success
				|| answer.resultRaw == -6)		// fail_by_user_kyc_deleted
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKKYC.UserStatusTopLevel(if(answer.resultRaw == 0)
																							 {
																								 KycUserStatusTopLevelGetResult.SUCCESS
																							 }
																							 else
																							 {
																								 KycUserStatusTopLevelGetResult.FAIL_BY_USER_NOT_FOUND
																							 },
																					 verificationLevel,
																					 userStatus,
																					 answer.createDt,
																					 answer.reviewCreateDt,
																					 answer.reviewCompleteDt))
			}
			else if(answer.resultRaw == -1
					|| answer.resultRaw == -2
					|| answer.resultRaw == -3) // FAIL_BY_TOKEN_INVALID/FAIL_BY_TOKEN_EXPIRED/FAIL_BY_ACCESS_DENIED
			{
				retry()
			}
			else
			{
				//-7 - fail_by_invalid_parameters
				//-5 - fail_by_billing
				//-4 - fail_by_service_temporary_not_valid

				failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
							  "Service temporary is not available.")
			}
		}
	}
}
