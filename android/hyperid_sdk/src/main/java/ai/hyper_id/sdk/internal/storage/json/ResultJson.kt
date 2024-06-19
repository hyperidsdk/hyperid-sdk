package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	ResultJson
					(
						@SerialName("result")
						@Required
						val result				: Int
					)
