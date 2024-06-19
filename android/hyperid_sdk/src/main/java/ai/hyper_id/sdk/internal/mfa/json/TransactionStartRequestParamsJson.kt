package ai.hyper_id.sdk.internal.mfa.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	TransactionStartRequestParamsJson
					(
						@SerialName("template_id")
						val templateId			:Int,

						@SerialName("values")
						val values 				: String?	= null,

						@SerialName("code")
						val code				: String?	= null
					)
