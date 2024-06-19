package ai.hyper_id.sdk.internal.auth.rest_api

import ai.hyper_id.sdk.IHyperIdSDK
import kotlinx.serialization.json.Json

internal abstract class RestApiRequest
{
	abstract fun start()
	protected abstract fun failWithError(error			: IHyperIdSDK.RequestResult,
										 errorDesc		: String? = null)
	protected abstract fun parseAnswerOrThrow(jsonString : String) : Any?
	protected abstract fun requestSuccess(answer : Any)
	//==================================================================================================
	//	retry
	//--------------------------------------------------------------------------------------------------
	fun retry()
	{
		if(++retryCounter < RETRY_COUNT_MAX)
		{
			start()
		}
		else
		{
			failWithError(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED)
		}
	}

	val requestResultListener =	object : IRestApiRequestResult
								{
									override fun OnRequestComplete(result				: RestApiRequestResult,
																   errorDesc			: String?,
																   requestAnswerBody	: String?)
									{
										when(result)
										{
											RestApiRequestResult.FAIL_AUTHORIZATION_REQUIRED	->
											{
												failWithError(IHyperIdSDK.RequestResult.FAIL_AUTHORIZATION_REQUIRED)
											}
											RestApiRequestResult.FAIL_CONNECTION				->
											{
												failWithError(IHyperIdSDK.RequestResult.FAIL_CONNECTION)
											}
											RestApiRequestResult.FAIL_SERVICE					->
											{
												failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE, errorDesc)
											}
											RestApiRequestResult.SUCCESS						->
											{
												if(requestAnswerBody != null)
												{
													parseJson(requestAnswerBody)
												}
												else
												{
													failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
																  "Service answer not valid. Answer is empty")
												}
											}
										}
									}
								}
	//==================================================================================================
	//	parseJson
	//--------------------------------------------------------------------------------------------------
	private fun parseJson(jsonString : String)
	{
		try
		{
			val answer = parseAnswerOrThrow(jsonString)
			if(answer != null)
			{
				requestSuccess(answer)
			}
			else
			{
				failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
							  "Service answer is not valid. Answer: [$jsonString]")
			}
		}
		catch(_exception : Exception)
		{
			failWithError(IHyperIdSDK.RequestResult.FAIL_SERVICE,
						  "Service JSON answer is not valid. Answer: [$jsonString]")
		}
	}

	protected	val jsonParser		= Json { ignoreUnknownKeys = true }
	private		var retryCounter	= 0

	companion object
	{
		@JvmStatic
		var idGenerator : Int =		-1
			get()
			{
				++field
				return field
			}
			private set
		private const val RETRY_COUNT_MAX = 2
	}
}
