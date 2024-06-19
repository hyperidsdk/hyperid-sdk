package ai.hyper_id.sdk.internal.auth.types

import android.net.Uri
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionSpec
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import java.util.concurrent.TimeUnit

//**************************************************************************************************
//	HTTPTransport
//--------------------------------------------------------------------------------------------------
internal class HTTPTransport
{
	//**************************************************************************************************
	//	TransportRequestResult
	//--------------------------------------------------------------------------------------------------
	enum class TransportRequestResult
	{
		ERROR_FAIL_CONNECTION,
		ERROR_FAIL_SERVER,

		SUCCESS,
	}
	//**************************************************************************************************
	//	ITransportRequestResult
	//--------------------------------------------------------------------------------------------------
	interface ITransportRequestResult
	{
		fun OnRequestComplete(_result				: TransportRequestResult,
							  _requestResultCode	: Int,
							  _requestAnswerBody	: String?)
	}
	//==================================================================================================
	//	done
	//--------------------------------------------------------------------------------------------------
	fun done()
	{
		httpClient.dispatcher.apply()
		{
			queuedCalls().forEach	{ it.cancel() }
			runningCalls().forEach	{ it.cancel() }
		}
	}
	//==================================================================================================
	//	requestGet
	//--------------------------------------------------------------------------------------------------
	fun requestGet(_uri				: Uri,
				   _accessToken		: String?,
				   _callback		: ITransportRequestResult)
	{
		val requestBuilder	= Request.Builder()
										.get()
										.url(_uri.toString())
		if(!_accessToken.isNullOrBlank())
		{
			requestBuilder.addHeader("Authorization", "Bearer $_accessToken")
		}

		val okhttpRequest	= httpClient.newCall(requestBuilder.build())
		okhttpRequest.enqueue(callbackPrepare(_callback))
	}
	//==================================================================================================
	//	requestPost
	//--------------------------------------------------------------------------------------------------
	fun requestPost(_uri			: Uri,
					_query			: List<Pair<String, String>>,
					_jsonContent	: String?,
					_accessToken	: String?,
					_callback		: ITransportRequestResult)
	{
		val postContent =	if(!_jsonContent.isNullOrBlank())
							{
								_jsonContent.toRequestBody("application/json; charset=utf-8".toMediaType())
							}
							else
							{
								FormBody.Builder()
										.apply()
										{
											_query.forEach()
											{
												add(it.first, it.second)
											}
										}
										.build()
							}
		val requestBuilder	= Request.Builder()
				.post(postContent)
				.url(_uri.toString())
		if(!_accessToken.isNullOrBlank())
		{
			requestBuilder.addHeader("Authorization", "Bearer $_accessToken")
		}

		val okhttpRequest = httpClient.newCall(requestBuilder.build())
		okhttpRequest.enqueue(callbackPrepare(_callback))
	}
	//==================================================================================================
	//	callbackPrepare
	//--------------------------------------------------------------------------------------------------
	private fun callbackPrepare(_callback : ITransportRequestResult) : Callback
	{
		return	object : Callback
				{
					private val callback = _callback
					//==================================================================================================
					//	onFailure
					//--------------------------------------------------------------------------------------------------
					override fun onFailure(call : Call, e : IOException)
					{
						Log.d(TAG, "[onFailure] ${e.localizedMessage}")

						callback.OnRequestComplete(TransportRequestResult.ERROR_FAIL_CONNECTION,
												   -1,
												   null)
					}
					//==================================================================================================
					//	onResponse
					//--------------------------------------------------------------------------------------------------
					override fun onResponse(call : Call, response : Response)
					{
						Log.d(TAG, "[onResponse]")

						val responseBody	= response.body?.string()
						val responseCode	= response.code
						val isSuccessful	= response.isSuccessful
						response.closeQuietly()

						callback.OnRequestComplete(	if(isSuccessful)
													{
														TransportRequestResult.SUCCESS
													}
													else
													{
														TransportRequestResult.ERROR_FAIL_SERVER
													},
													responseCode,
													responseBody)
					}
				}
	}


	private val httpClient	= OkHttpClient.Builder()
									.connectionSpecs(listOf(ConnectionSpec.MODERN_TLS,
															ConnectionSpec.COMPATIBLE_TLS))
									.callTimeout(10L, TimeUnit.SECONDS)
									.connectTimeout(10L, TimeUnit.SECONDS)
									.readTimeout(10L, TimeUnit.SECONDS)
									.writeTimeout(10L, TimeUnit.SECONDS)
									.build()

	companion object
	{
		@Suppress("unused")
		private const val TAG	= "HTTPTransport"
	}
}
