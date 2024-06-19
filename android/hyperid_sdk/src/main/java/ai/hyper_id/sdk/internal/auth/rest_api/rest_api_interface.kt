package ai.hyper_id.sdk.internal.auth.rest_api

internal enum class RestApiRequestResult
{
	FAIL_AUTHORIZATION_REQUIRED,
	FAIL_CONNECTION,
	FAIL_SERVICE,

	SUCCESS,
}

//**************************************************************************************************
//	IRestApiRequestResult
//--------------------------------------------------------------------------------------------------
internal interface IRestApiRequestResult
{
	fun OnRequestComplete(result			: RestApiRequestResult,
						  errorDesc			: String?,
						  requestAnswerBody	: String?)
}

internal interface IRestApiInterface
{
	fun restApiRequestPost(urlPath			: String,
						   queryParameters	: List<Pair<String, String>>,
						   callback			: IRestApiRequestResult)
	fun restApiRequestPost(urlPath			: String,
						   jsonContent		: String,
						   callback			: IRestApiRequestResult)
}
