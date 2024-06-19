package ai.hyper_id.sdk.internal.mfa.json

import ai.hyper_id.sdk.api.mfa.enums.TransactionCompleteResult
import ai.hyper_id.sdk.api.mfa.enums.TransactionStatus
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//**************************************************************************************************
//	TransactionStatusCheckJson
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class	TransactionStatusCheckJson
					(
						@SerialName("result")
						@Required
						val result									: Int,

						@SerialName("transaction_id")
						@Required
						val transactionId							: Int,

						@SerialName("transaction_status")
						@Required
						private val transactionStatusRaw			: Int,

						@SerialName("transaction_complete_result")
						@Required
						private val transactionCompleteResultRaw	: Int
					)
{
	val transactionStatus			=	when(transactionStatusRaw)
										{
											0		-> TransactionStatus.WAIT_USER_ACTION
											1		-> TransactionStatus.USER_COMPLETE_ACTION
											2		-> TransactionStatus.EXPIRED
											4		-> TransactionStatus.USER_CANCELLED_ACTION
											else	-> TransactionStatus.EXPIRED
										}
	val transactionCompleteResult	=	when(transactionCompleteResultRaw)
										{
											0		-> TransactionCompleteResult.APPROVED
											1		-> TransactionCompleteResult.DENIED
											else	-> TransactionCompleteResult.DENIED
										}
}
