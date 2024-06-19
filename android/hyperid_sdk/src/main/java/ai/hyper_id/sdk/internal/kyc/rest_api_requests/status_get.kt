package ai.hyper_id.sdk.internal.kyc.rest_api_requests

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.kyc.IHyperIDSDKKYC
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatus
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatusGetResult
import ai.hyper_id.sdk.internal.auth.rest_api.RestApiRequest
import ai.hyper_id.sdk.internal.kyc.HyperIDSDKKYCImpl
import ai.hyper_id.sdk.internal.kyc.json.StatusGetResponseJson

//**************************************************************************************************
//	RequestUserStatusGet
//--------------------------------------------------------------------------------------------------
internal class RequestUserStatusGet(private	val sdk					: HyperIDSDKKYCImpl,
									val requestId					: Int,
									val kycVerificationLevel		: KycVerificationLevel,
									val completeListener			: IHyperIDSDKKYC.IUserStatusGetCompleteListener) : RestApiRequest()
{
	override fun start()
	{
		sdk.getUserStatus(this)
	}
	override fun failWithError(error : IHyperIdSDK.RequestResult, errorDesc : String?)
	{
		completeListener.onRequestComplete(error, errorDesc, null)
	}
	override fun parseAnswerOrThrow(jsonString : String) : StatusGetResponseJson
	{
		return jsonParser.decodeFromString(jsonString)
	}
	override fun requestSuccess(answer : Any)
	{
		if(answer is StatusGetResponseJson)
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
			if (answer.resultRaw == 0			//success
				|| answer.resultRaw == -7		//fail by user not found
				|| answer.resultRaw == -8 )		//fail by user kyc deleted
			{
				completeListener.onRequestComplete(IHyperIdSDK.RequestResult.SUCCESS,
												   null,
												   IHyperIDSDKKYC.UserStatus(when(answer.resultRaw)
																			 {
																				 -7		-> KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND
																				 -8		-> KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND
																				 else	-> KycUserStatusGetResult.SUCCESS
																			 },
																			 verificationLevel,
																			 userStatus,
																			 answer.kycId,
																			 answer.firstName,
																			 answer.lastName,
																			 answer.birthday,
																			 answer.countryA2,
																			 answer.countryA3,
																			 answer.providedCountryA2,
																			 answer.providedCountryA3,
																			 answer.addressCountryA2,
																			 answer.addressCountryA3,
																			 answer.phoneNumberCountryA2,
																			 answer.phoneNumberCountryA3,
																			 answer.phoneNumberCountryCode,
																			 answer.ipCountriesA2,
																			 answer.ipCountriesA3,
																			 answer.moderationComment,
																			 answer.rejectReasons,
																			 answer.supportLink,
																			 answer.createDt,
																			 answer.reviewCreateDt,
																			 answer.reviewCompleteDt,
																			 answer.expirationDt))
			}
			else if(answer.resultRaw == -1
					|| answer.resultRaw == -2
					|| answer.resultRaw == -3)	// token
			{
				retry()
			}
			else
			{
				//-4	fail by service temporary not valid
				//-5	fail by invalid parameters
				//-6 	fail by billing

				failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
							  "Service temporary is not available.")
			}
		}
	}
}
