package ai.hyper_id.sdk.internal.auth.types

import ai.hyper_id.sdk.api.auth.model.AuthorizationMethod
import ai.hyper_id.sdk.api.auth.model.ClientInfo
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

//==================================================================================================
//	Utils
//--------------------------------------------------------------------------------------------------
internal object Utils
{
	//==================================================================================================
	//	tokensObtainParametersPrepare
	//--------------------------------------------------------------------------------------------------
	internal fun tokensObtainParametersPrepare(_clientInfo	: ClientInfo,
											   _discover	: Discover,
											   _code		: String)		: List<Pair<String, String>>
	{
		val params = mutableListOf<Pair<String, String>>()
		params.add(Pair("grant_type",		 JwtNames.queryValueAuthorizationCode))
		params.add(Pair("redirect_uri",		_clientInfo.redirectUri))
		params.add(Pair("code",				_code))

		return params + authorizationParams(_clientInfo, _discover)
	}
	//==================================================================================================
	//	accessTokenRefreshParametersPrepare
	//--------------------------------------------------------------------------------------------------
	internal fun accessTokenRefreshParametersPrepare(_clientInfo	: ClientInfo,
													 _discover		: Discover,
													 _refreshToken	: String)	: List<Pair<String, String>>
	{
		val params = mutableListOf<Pair<String, String>>()
		params.add(Pair("grant_type",			"refresh_token"))
		params.add(Pair("refresh_token",		_refreshToken))
		params.add(Pair("redirect_uri",			_clientInfo.redirectUri))

		return params + authorizationParams(_clientInfo, _discover)
	}
	//==================================================================================================
	//	logoutParametersPrepare
	//--------------------------------------------------------------------------------------------------
	internal fun logoutParametersPrepare(_clientInfo	: ClientInfo,
										 _discover		: Discover,
										 _refreshToken	: String)	: List<Pair<String, String>>
	{
		val params = mutableListOf<Pair<String, String>>()
		params.add(Pair("refresh_token",	_refreshToken))

		return params + authorizationParams(_clientInfo, _discover)
	}
	//==================================================================================================
	//	authorizationParams
	//--------------------------------------------------------------------------------------------------
	private fun authorizationParams(_clientInfo : ClientInfo,
									_discover	: Discover)		: List<Pair<String, String>>
	{
		val authorizationParams = mutableListOf<Pair<String, String>>()
		when(_clientInfo.authorizationMethod)
		{
			AuthorizationMethod.CLIENT_SECRET ->
			{
				authorizationParams.add(Pair("client_id",			_clientInfo.clientId))
				authorizationParams.add(Pair("client_secret",		_clientInfo.clientSecret!!))
			}
			AuthorizationMethod.CLIENT_SECRET_HS256,
			AuthorizationMethod.CLIENT_RS256  ->
			{
				authorizationParams.add(Pair("client_assertion_type",	JwtNames.queryValueClientAssertionType))
				authorizationParams.add(Pair("client_assertion",		assertionTokenGenerate(_clientInfo, _discover)))
			}
		}
		return authorizationParams
	}
	//==================================================================================================
	//	assertionTokenGenerate
	//--------------------------------------------------------------------------------------------------
	private fun assertionTokenGenerate(_clientInfo	: ClientInfo,
									   _discover	: Discover)		: String
	{
		val tokenHeader			= eJwtTokenHeader()
		tokenHeader.alg			= when(_clientInfo.authorizationMethod)
									{
										AuthorizationMethod.CLIENT_SECRET			-> "HA256"
										AuthorizationMethod.CLIENT_SECRET_HS256		-> "HS256"
										AuthorizationMethod.CLIENT_RS256			-> "RS256"
									}

		val tokenPayload		= eJwtTokenPayload()
		tokenPayload.sub		= _clientInfo.clientId
		tokenPayload.jti		= UUID.randomUUID().toString()
		tokenPayload.issuer		= _clientInfo.clientId
		tokenPayload.audience	= _discover.issuer

		val headerJson			= Json.encodeToString(tokenHeader)
		val payloadJson			= Json.encodeToString(tokenPayload)

		val algorithm			=	if(_clientInfo.authorizationMethod == AuthorizationMethod.CLIENT_SECRET_HS256)
									{
										Algorithm.HMAC256(_clientInfo.clientSecret)
									}
									else
									{
										Algorithm.RSA256(_clientInfo.privateRSAKey)
									}
		return	try
				{
					JWT.create().withHeader(headerJson).withPayload(payloadJson).sign(algorithm)
				}
				catch(_exception : Exception)
				{
					""
				}
	}

	//**************************************************************************************************
	//	eJwtTokenHeader
	//--------------------------------------------------------------------------------------------------
	@Serializable
	private class eJwtTokenHeader
	{
		@SerialName("alg")
		@Required
		var alg				: String?		= null

		@OptIn(ExperimentalSerializationApi::class)
		@SerialName("typ")
		@EncodeDefault
		var typ				: String?		= "JWT"
	}
	//**************************************************************************************************
	//	eJwtTokenPayload
	//--------------------------------------------------------------------------------------------------
	@Serializable
	private class eJwtTokenPayload
	{
		@SerialName("sub")
		var sub							: String?		= null

		@SerialName("jti")
		var jti							: String?		= null

		@OptIn(ExperimentalSerializationApi::class)
		@SerialName("iat")
		@EncodeDefault
		var iat							: Long			= TimeCurrent()

		@OptIn(ExperimentalSerializationApi::class)
		@SerialName("exp")
		@EncodeDefault
		var exp							: Long			= iat + (20 * 60)

		@SerialName("iss")
		var issuer						: String?		= null

		@SerialName("aud")
		var audience					: String?		= null

		//==================================================================================================
		//	TimeCurrent
		//--------------------------------------------------------------------------------------------------
		private fun TimeCurrent() : Long
		{
			val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
			return (calendar.timeInMillis / 1000)
		}
	}
}
