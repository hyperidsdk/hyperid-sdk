package ai.hyper_id.sdk.internal.kyc.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	StatusGetResponseJson
					(
						@SerialName("result")
						@Required
						val resultRaw							: Int,

						@SerialName("verification_level")
						@Required
						val verificationLevelRaw				: Int,

						@SerialName("user_status")
						@Required
						val userStatus							: Int,

						@SerialName("kyc_id")
						@Required
						val kycId								: String,

						@SerialName("first_name")
						val firstName							: String?		= null,

						@SerialName("last_name")
						val lastName							: String?		= null,

						@SerialName("birthday")
						val birthday							: String?		= null,

						@SerialName("country_a2")
						val countryA2							: String?		= null,

						@SerialName("country_a3")
						val countryA3							: String?		= null,

						@SerialName("provided_country_a2")
						val providedCountryA2					: String?		= null,

						@SerialName("provided_country_a3")
						val providedCountryA3					: String?		= null,

						@SerialName("address_country_a2")
						val addressCountryA2					: String?		= null,

						@SerialName("address_country_a3")
						val addressCountryA3					: String?		= null,

						@SerialName("phone_number_country_a2")
						val phoneNumberCountryA2				: String?		= null,

						@SerialName("phone_number_country_a3")
						val phoneNumberCountryA3				: String?		= null,

						@SerialName("phone_number_country_code")
						val phoneNumberCountryCode				: String?		= null,

						@SerialName("ip_countries_a2")
						val ipCountriesA2						: List<String>	= emptyList(),

						@SerialName("ip_countries_a3")
						val ipCountriesA3						: List<String>	= emptyList(),

						@SerialName("moderation_comment")
						val moderationComment					: String?		= null,

						@SerialName("reject_reasons")
						val rejectReasons						: List<String>	= emptyList(),

						@SerialName("support_link")
						val supportLink							: String?		= null,

						@SerialName("create_dt")
						val createDt							: Long			= 0L,

						@SerialName("review_create_dt")
						val reviewCreateDt						: Long			= 0L,

						@SerialName("review_complete_dt")
						val reviewCompleteDt					: Long			= 0L,

						@SerialName("expiration_dt")
						val expirationDt						: Long			= 0L
					)
