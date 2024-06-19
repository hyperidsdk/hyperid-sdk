package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	UserDataKeysGetResultJson
					(
						@SerialName("result")
						@Required
						val result						: Int,

						@SerialName("keys_public")
						@Required
						val keysPublic					: List<String>,

						@SerialName("keys_private")
						@Required
						val keysPrivate					: List<String>,
					)
