package ai.hyper_id.sdk.internal.kyc.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	StatusTopLevelGetResponseJson
					(
						@SerialName("request_id")
						@Required
						val requestId							: Long,

						@SerialName("verification_level")
						@Required
						val verificationLevelRaw				: Int,

						@SerialName("user_status")
						@Required
						val userStatus							: Int,

						@SerialName("create_dt")
						@Required
						val createDt							: Long,

						@SerialName("review_create_dt")
						@Required
						val reviewCreateDt						: Long,

						@SerialName("review_complete_dt")
						@Required
						val reviewCompleteDt					: Long,

						@SerialName("result")
						@Required
						val resultRaw							: Int
					)
