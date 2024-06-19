package ai.hyper_id.sdk.internal.auth.json

import ai.hyper_id.sdk.api.auth.model.UserInfo
import ai.hyper_id.sdk.api.auth.model.WalletInfo
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//==================================================================================================
//	eUserInfoJson
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class	UserInfoJson
					(
						@SerialName("sub")
						@Required
						val userId						: String?		= null,

						@SerialName("wallet_address")
						val walletAddress				: String?		= null,

						@SerialName("wallet_chain_id")
						val walletChainId				: String?		= null,

						@SerialName("email_verified")
						val isEmailVerified				: Boolean		= false,

						@SerialName("email")
						val email						: String?		= null,

						@SerialName("preferred_username")
						val preferredUserName			: String?		= null,

						@SerialName("given_name")
						val givenName					: String?		= null,

						@SerialName("name")
						val name						: String?		= null,

						@SerialName("device_id")
						val deviceId					: String?		= null,

						@SerialName("telegram_user_first_name")
						val telegramUserFirstName		: String?		= null,

						@SerialName("telegram_user_last_name")
						val telegramUserLLastNAme		: String?		= null,

						@SerialName("telegram_user_id")
						val telegramUserId				: String?		= null,

						@SerialName("telegram_user_name")
						val telegramUserName			: String?		= null,

						@SerialName("twitter_user_id")
						val twitterUserId				: String?		= null,

						@SerialName("twitter_user_name")
						val twitterUserName				: String?		= null,

						@SerialName("microsoft_user_id")
						val microsoftUserId				: String?		= null,

						@SerialName("microsoft_user_email")
						val microsoftUserEmail			: String?		= null,

						@SerialName("microsoft_user_tenant_id")
						val microsoftUserTenantId		: String?		= null,

						@SerialName("region")
						val region						: String?		= null,

						@SerialName("ip")
						val ip							: String?		= null,

						@SerialName("location_country")
						val locationCountry				: String?		= null,

						@SerialName("location_province")
						val locationProvince			: String?		= null,

						@SerialName("mfa_app_url")
						val mfaAppUrl					: String?		= null,

						@SerialName("sub_project")
						val subProject					: String?		= null,

						@SerialName("vendor_device_id")
						val vendorDeviceId				: String?		= null,

						@SerialName("verification_level")
						val verificationLevel			: String?		= null,

						@SerialName("wallet_source")
						val walletSourceId				: Int?			= null,

						@SerialName("is_wallet_verified")
						val isWalletVerified			: Boolean		= false,

						@SerialName("wallet_family")
						val walletFamilyId				: Int?			= null,

						@SerialName("wallet_tags")
						val walletTags					: String?		= null,

						@SerialName("family_name")
						val familyName					: String?		= null,

						@SerialName("locale")
						val locale						: String?		= null,

						@SerialName("phone_number")
						val phoneNumber					: String?		= null,

						@SerialName("phone_number_verified")
						val isPhoneNumberVerified		: Boolean		= false,

						@SerialName("updated_at")
						val updatedAt					: Long			= 0L,

						@SerialName("is_guest")
						val isGuest						: Boolean		= false)
{
	//==================================================================================================
	//	toUserInfo
	//--------------------------------------------------------------------------------------------------
	fun toUserInfo() : UserInfo
	{
		return UserInfo(userId,
						isGuest,
						email,
						isEmailVerified,
						deviceId,
						ip,
						if(walletAddress != null)
						{
							WalletInfo(walletAddress,
									   walletChainId,
									   walletSourceId,
									   isWalletVerified,
									   walletFamilyId,
									   walletTags)
						}
						else
						{
							null
						})
	}
}
