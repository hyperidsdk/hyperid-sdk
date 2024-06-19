package ai.hyper_id.sdk.internal.mfa.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//**************************************************************************************************
//	TransactionCancelJson
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class TransactionCancelJson
					(
						@SerialName("result")
						@Required
						val result						: Int,

						@SerialName("transaction_id")
						@Required
						private val transactionId		: Int,
					)
