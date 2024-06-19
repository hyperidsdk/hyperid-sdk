package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDataGetEntryJson
					(
						@SerialName("value_key")
						@Required
						val key					: String,

						@SerialName("value_data")
						@Required
						val value				: String
					)

@Serializable
internal data class	UserDataGetResultJson
					(
						@SerialName("result")
						@Required
						val result				: Int,

						@SerialName("values")
						@Required
						val values				: List<UserDataGetEntryJson>
					)
