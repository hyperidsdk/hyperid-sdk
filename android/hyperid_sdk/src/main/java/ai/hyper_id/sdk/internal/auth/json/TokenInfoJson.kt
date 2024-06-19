package ai.hyper_id.sdk.internal.auth.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	TokenInfoJson
					(
						@SerialName("access_token")
						@Required
						val accessTokenRaw			: String?,

						@SerialName("expires_in")
						@Required
						val accessTokenValidTo		: Long,

						@SerialName("refresh_token")
						@Required
						val refreshTokenRaw			: String?,

						@SerialName("refresh_expires_in")
						@Required
						val refreshTokenValidTo		: Long
					)
