package ai.hyper_id.sdk.api.kyc

import ai.hyper_id.sdk.IHyperIdSDK
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatus
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatusGetResult
import ai.hyper_id.sdk.api.kyc.enums.KycUserStatusTopLevelGetResult

/**
 * IHyperIDSDKKYC
 *
 * **/
interface IHyperIDSDKKYC
{
	data class UserStatus(val result					: KycUserStatusGetResult,
						  val verificationLevel			: KycVerificationLevel,
						  val userStatus				: KycUserStatus,
						  val kycId						: String?,
						  val firstName					: String?,
						  val lastName					: String?,
						  val birthday					: String?,
						  val countryA2					: String?,
						  val countryA3					: String?,
						  val providedCountryA2			: String?,
						  val providedCountryA3			: String?,
						  val addressCountryA2			: String?,
						  val addressCountryA3			: String?,
						  val phoneNumberCountryA2		: String?,
						  val phoneNumberCountryA3		: String?,
						  val phoneNumberCountryCode	: String?,
						  val ipCountriesA2				: List<String>,
						  val ipCountriesA3				: List<String>,
						  val moderationComment			: String?,
						  val rejectReasons				: List<String>,
						  val supportLink				: String?,
						  val createDt					: Long,
						  val reviewCreateDt			: Long,
						  val reviewCompleteDt			: Long,
						  val expirationDt				: Long)
	interface IUserStatusGetCompleteListener {
		fun onRequestComplete(result 			: IHyperIdSDK.RequestResult,
							  errorDesc			: String?,
							  response			: UserStatus?)
	}

	data class UserStatusTopLevel(val result				: KycUserStatusTopLevelGetResult,
								  val verificationLevel		: KycVerificationLevel,
								  val userStatus			: KycUserStatus,
								  val createDt				: Long,
								  val reviewCreateDt		: Long,
								  val reviewCompleteDt		: Long)
	interface IUserStatusTopLevelGetCompleteListener {
		fun onRequestComplete(result 		: IHyperIdSDK.RequestResult,
							  errorDesc		: String?,
							  response		: UserStatusTopLevel?)
	}

	/**
	 * userStatusGet
	**/
	fun getUserStatus(kycVerificationLevel		: KycVerificationLevel,
					  completeListener			: IUserStatusGetCompleteListener)

	/**
	 * userStatusTopLevelGet
	 *
	 */
	fun getUserStatusTopLevel(completeListener	: IUserStatusTopLevelGetCompleteListener)
}
