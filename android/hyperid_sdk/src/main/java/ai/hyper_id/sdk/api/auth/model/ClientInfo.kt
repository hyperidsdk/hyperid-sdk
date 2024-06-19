package ai.hyper_id.sdk.api.auth.model

import java.security.interfaces.RSAPrivateKey

//==================================================================================================
//	eClientInfo
//--------------------------------------------------------------------------------------------------
data class ClientInfo(val clientId				: String,
					  val redirectUri			: String,
					  val authorizationMethod	: AuthorizationMethod,
					  val clientSecret			: String?,
					  val privateRSAKey			: RSAPrivateKey?)
{
	//==================================================================================================
	//	isValid
	//--------------------------------------------------------------------------------------------------
	fun isValid() : Boolean
	{
		return	clientId.isNotBlank()
			&&	redirectUri.isNotBlank()
			&&	when(authorizationMethod)
				{
					AuthorizationMethod.CLIENT_SECRET,
					AuthorizationMethod.CLIENT_SECRET_HS256 -> !clientSecret.isNullOrBlank()
					AuthorizationMethod.CLIENT_RS256        -> privateRSAKey != null
				}
	}
	//==================================================================================================
	//	toString
	//--------------------------------------------------------------------------------------------------
	override fun toString() : String
	{
		return "[ClientInfo clientId/$clientId, " +
			   "redirectUri/$redirectUri, " +
			   "authorizationMethod/$authorizationMethod, " +
			   "clientSecret/$clientSecret, ]"
	}
}
