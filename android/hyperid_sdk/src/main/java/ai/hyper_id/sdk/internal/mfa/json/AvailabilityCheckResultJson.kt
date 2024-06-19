package ai.hyper_id.sdk.internal.mfa.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//**************************************************************************************************
//	eMFAAvailabilityCheckResult
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class	AvailabilityCheckResultJson
					(
						@SerialName("result")
						@Required
						val result					: Int,

						@SerialName("is_available")
						@Required
						val isAvailable				: Boolean
					)
