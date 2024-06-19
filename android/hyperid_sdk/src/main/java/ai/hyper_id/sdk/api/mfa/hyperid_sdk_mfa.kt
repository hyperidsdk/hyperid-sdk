package ai.hyper_id.sdk.api.mfa

import ai.hyper_id.sdk.api.mfa.enums.TransactionCompleteResult
import ai.hyper_id.sdk.api.mfa.enums.TransactionStatus

interface IHyperIdSDKMFA
{
	enum class HyperIDMFARequestResult
	{
		FAIL_INIT_REQUIRED,
		FAIL_AUTHORIZATION_REQUIRED,
		FAIL_MFA_NOT_AVAILABLE,			//all except availability check
		FAIL_TRANSACTION_NOT_FOUND,		//only for status check
		FAIL_CONNECTION,
		FAIL_SERVICE,
		SUCCESS,
	}

	interface IAvailabilityCheckResultListener
	{
		fun onRequestComplete(result			: HyperIDMFARequestResult,
							  serviceErrorDesc	: String?,
							  isAvailable		: Boolean)
	}
	interface ITransactionStartResultListener
	{
		fun onRequestComplete(result			: HyperIDMFARequestResult,
							  serviceErrorDesc	: String?,
							  transactionId		: Int)
	}
	interface ITransactionStatusCheckResultListener
	{
		fun onRequestComplete(result					: HyperIDMFARequestResult,
							  serviceErrorDesc			: String?,
							  transactionId				: Int,
							  transactionStatus			: TransactionStatus?,
							  transactionCompleteResult	: TransactionCompleteResult?)
	}
	interface ITransactionCancelResultListener
	{
		fun onRequestComplete(result			: HyperIDMFARequestResult,
							  serviceErrorDesc	: String?,
							  transactionId		: Int)

	}

	/**
	 * availabilityCheck
	 *
	 * This check is required before call requests functionality to ensure its availability
	 * @param	completeListener	callback with result
	**/
	fun	availabilityCheck(completeListener		: IAvailabilityCheckResultListener)


	/**
	 * transactionStart
	 *
	 * This check is required before call requests functionality to ensure its availability
	 * @param	question			specific user request data. Will be shown insde transaction on HyperID Authenticator
	 * @param	code				control code to show user in HyperIdAuthenticator app. Must contain only 2 symbols.
	 * @param	completeListener	callback with result
	 **/
	fun transactionStart(question			: String,
						 code				: String,
						 completeListener	: ITransactionStartResultListener)


	/**
	 * TransactionStatusCheck
	 *
	 * Check transaction status to ensure user action in HyperIdAuthenticator app
	 *
	 * @param	transactionId		transaction ID obtained in RequestStart result
	 * @param	completeListener	callback with result
	 **/
	fun transactionStatusCheck(transactionId	: Int,
							   completeListener	: ITransactionStatusCheckResultListener)


	/**
	 * TransactionCancel
	 *
	 * Cancel transaction for HyperIdAuthenticator app
	 *
	 * @param	transactionId		transaction ID obtained in RequestStart result
	 * @param	completeListener	callback with result
	 **/
	fun transactionCancel(transactionId			: Int,
						  completeListener		: ITransactionCancelResultListener)
}
