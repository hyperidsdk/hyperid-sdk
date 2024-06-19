package ai.hyper_id.sdk.internal.mfa.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//**************************************************************************************************
//	TransactionStartResultJson
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class	TransactionStartResultJson
					(
						@SerialName("result")
						@Required
						val result					: Int,

						@SerialName("transaction_id")
						@Required
						val transactionId			: Int,
					)
