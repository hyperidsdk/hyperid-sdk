package com.hyper_id.sdk.api

import ai.hyper_id.sdk.api.mfa.IHyperIdSDKMFA
import ai.hyper_id.sdk.api.mfa.enums.TransactionCompleteResult
import ai.hyper_id.sdk.api.mfa.enums.TransactionStatus
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HyperIdSdkMFADemoApi(private val mfaApi : IHyperIdSDKMFA)
{
	enum class HyperIdSDKMFAState
	{
		CREATED,
		AVAILABLE,
		NOT_AVAILABLE,
	}

	//==================================================================================================
	//	done
	//--------------------------------------------------------------------------------------------------
	fun done()
	{
//		sdkMfa.done()

		state			= HyperIdSDKMFAState.CREATED
		transactionId	= -1
	}
	//==================================================================================================
	//	checkAvailability
	//--------------------------------------------------------------------------------------------------
	fun checkAvailability()
	{
		if(state == HyperIdSDKMFAState.CREATED)
		{
			val completeListener = object : IHyperIdSDKMFA.IAvailabilityCheckResultListener
				{
					override fun onRequestComplete(_result				: IHyperIdSDKMFA.HyperIDMFARequestResult,
												   _serviceErrorDesc	: String?,
												   _isAvailable			: Boolean)
					{
						state =	if(_result == IHyperIdSDKMFA.HyperIDMFARequestResult.SUCCESS)
								{
									Log.d(TAG, "[checkAvailability] _isAvailable/$_isAvailable")

									if(_isAvailable)
									{
										HyperIdSDKMFAState.AVAILABLE
									}
									else
									{
										HyperIdSDKMFAState.NOT_AVAILABLE
									}
								}
								else
								{
									Log.d(TAG, "[checkAvailability] failed with result/$_result($_serviceErrorDesc) ")

									HyperIdSDKMFAState.CREATED
								}
					}
				}

//			sdkMfa.availabilityCheck(completeListener)
		}
		else
		{
			Log.d(TAG, "[checkAvailability] sdk not initialised yet")
		}
	}
	//==================================================================================================
	//	transactionStart
	//--------------------------------------------------------------------------------------------------
	fun transactionStart()
	{
		if(state == HyperIdSDKMFAState.AVAILABLE)
		{
			val completeListener = object : IHyperIdSDKMFA.ITransactionStartResultListener
					{
						override fun onRequestComplete(_result				: IHyperIdSDKMFA.HyperIDMFARequestResult,
													   _serviceErrorDesc	: String?,
													   _transactionId		: Int)
						{
							if(_result == IHyperIdSDKMFA.HyperIDMFARequestResult.SUCCESS)
							{
								Log.d(TAG, "[transactionStart] _transactionId/$_transactionId")

								transactionId = _transactionId
							}
							else
							{
								Log.d(TAG, "[transactionStart] failed with result/$_result($_serviceErrorDesc)")
							}
						}
					}

//			sdkMfa.transactionStart("Change security",
//									"19",
//									completeListener)
		}
		else
		{
			Log.d(TAG, "[transactionStart] sdk not initialised yet")
		}
	}
	fun transactionCancel()
	{
		if(state == HyperIdSDKMFAState.AVAILABLE)
		{
			val completeListener = object : IHyperIdSDKMFA.ITransactionCancelResultListener
					{
						override fun onRequestComplete(_result				: IHyperIdSDKMFA.HyperIDMFARequestResult,
													   _serviceErrorDesc	: String?,
													   _transactionId		: Int)
						{
							Log.d(TAG, "[transactionCancel] completed with result/$_result($_serviceErrorDesc) and _transactionId/$_transactionId")

							transactionId = -1
						}
					}

//			sdkMfa.transactionCancel(transactionId,
//									 completeListener)
		}
		else
		{
			Log.d(TAG, "[transactionCancel] sdk not initialised yet")
		}
	}
	fun transactionStatusCheck()
	{
		if(state == HyperIdSDKMFAState.AVAILABLE)
		{
			val completeListener = object : IHyperIdSDKMFA.ITransactionStatusCheckResultListener
					{
						override fun onRequestComplete(_result						: IHyperIdSDKMFA.HyperIDMFARequestResult,
													   _serviceErrorDesc			: String?,
													   _transactionId				: Int,
													   _transactionStatus			: TransactionStatus?,
													   _transactionCompleteResult	: TransactionCompleteResult?)
						{
							Log.d(TAG, "[transactionCancel] completed with result/$_result($_serviceErrorDesc)")

							Log.d(TAG, "[transactionCancel] _transactionId/$_transactionId")
							Log.d(TAG, "[transactionCancel] _transactionStatus/$_transactionStatus")
							Log.d(TAG, "[transactionCancel] _transactionCompleteResult/$_transactionCompleteResult")

							_transactionStatus?.also()
							{
								when(it)
								{
									TransactionStatus.WAIT_USER_ACTION      -> { /* do nothing wait user action */ }

									TransactionStatus.USER_COMPLETE_ACTION  -> transactionId = -1

									TransactionStatus.USER_CANCELLED_ACTION -> transactionId = -1
									TransactionStatus.EXPIRED               -> transactionId = -1
								}
							}
						}
					}

//			sdkMfa.transactionStatusCheck(transactionId, completeListener)
		}
		else
		{
			Log.d(TAG, "[transactionCancel] sdk not initialised yet")
		}
	}

//	private val sdkMfa			: IHyperIdSDKMFA = IHyperIdSdkMFACreate()
	private var state								= HyperIdSDKMFAState.CREATED
		private set(_value)
		{
			if(field != _value)
			{
				field				= _value
				stateFlow_.value	= field
			}
		}
	private var transactionId						= -1
		private set(_value)
		{
			if(field != _value)
			{
				field						= _value
				transactionIdFlow_.value	= field
			}
		}

	private val stateFlow_							= MutableStateFlow(HyperIdSDKMFAState.CREATED)
	val stateFlow									= stateFlow_.asStateFlow()

	private val transactionIdFlow_					= MutableStateFlow(transactionId)
	val transactionIdFlow							= transactionIdFlow_.asStateFlow()

	companion object
	{
		@Suppress("unused")
		private const val TAG	= "HyperIdSdkMFADemoApi"
	}
}
