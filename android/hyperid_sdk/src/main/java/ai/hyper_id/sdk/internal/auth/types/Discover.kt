package ai.hyper_id.sdk.internal.auth.types

//**************************************************************************************************
//	Discover
//--------------------------------------------------------------------------------------------------
data class	Discover
			(
				val authEndpoint			: String,
				val tokenEndpoint			: String,
				val introspectionEndpoint	: String,
				val userInfoEndpoint		: String,
				val revokeEndpoint			: String,
				val endSessionEndpoint		: String,
				val restApiTokenEndpoint	: String,
				val restApiPublicEndpoint	: String,
				val issuer					: String,
				val walletChains			: List<Int>,
				val scopes					: List<String>,
				val identityProviders		: List<String>
			)
{
	//==================================================================================================
	//	IsValid
	//--------------------------------------------------------------------------------------------------
	fun isValid() : Boolean
	{
		return authEndpoint.isNotBlank()
			&& tokenEndpoint.isNotBlank()
			&& introspectionEndpoint.isNotBlank()
			&& userInfoEndpoint.isNotBlank()
			&& revokeEndpoint.isNotBlank()
			&& restApiTokenEndpoint.isNotBlank()
			&& restApiPublicEndpoint.isNotBlank()
			&& issuer.isNotBlank()
			&& scopes.isNotEmpty()
	}
	//==================================================================================================
	//	toString
	//--------------------------------------------------------------------------------------------------
	override fun toString() : String
	{
		return "[eDiscover] : [endpoints:/[authEndpoint$authEndpoint," +
			   "tokenEndpoint$tokenEndpoint, " +
			   "introspectionEndpoint$introspectionEndpoint, " +
			   "userInfoEndpoint$userInfoEndpoint, " +
			   "revokeEndpoint$revokeEndpoint, " +
			   "restApiTokenEndpoint$restApiTokenEndpoint, " +
			   "restApiPublicEndpoint$restApiPublicEndpoint, " +
			   "issuer$issuer], " +
			   "walletChains/[${walletChains.joinToString()}], " +
			   "scopes/[${scopes.joinToString()}], " +
			   "identityProviders/[${identityProviders.joinToString(" ")}]]"
	}
}
