package ai.hyper_id.sdk.internal.auth.json

import ai.hyper_id.sdk.internal.auth.types.Discover
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//**************************************************************************************************
//	eDiscoverJson
//--------------------------------------------------------------------------------------------------
@Serializable
internal data class	DiscoverJson
					(
						@SerialName("authorization_endpoint")
						@Required
						val authEndpoint					: String,

						@SerialName("token_endpoint")
						@Required
						val tokenEndpoint					: String,

						@SerialName("introspection_endpoint")
						@Required
						val introspectionEndpoint			: String,

						@SerialName("userinfo_endpoint")
						@Required
						val userInfoEndpoint				: String,

						@SerialName("revocation_endpoint")
						@Required
						val revokeEndpoint					: String,

						@SerialName("rest_api_token_endpoint")
						@Required
						val restApiTokenEndpoint			: String,

						@SerialName("rest_api_public_endpoint")
						@Required
						val restApiPublicEndpoint			: String,

						@SerialName("end_session_endpoint")
						@Required
						val endSessionEndpoint				: String,

						@SerialName("issuer")
						@Required
						val issuer							: String,

						@SerialName("wallet_chain")
						@Required
						val walletChains					: List<Int>,

						@SerialName("client_scopes_default")
						@Required
						val defaultScopes					: List<String>,

						@SerialName("client_scopes_optional")
						@Required
						val optionalScopes					: List<String>,

						@SerialName("identity_providers")
						@Required
						val identityProviders				: List<String>,

						@SerialName("code_challenge_methods_supported")
						@Required
						val codeChallengeMethodsSupported	: List<String>
					)
{
	//==================================================================================================
	//	toDiscover
	//--------------------------------------------------------------------------------------------------
	fun toDiscover() : Discover
	{
		return Discover(authEndpoint,
						tokenEndpoint,
						introspectionEndpoint,
						userInfoEndpoint,
						revokeEndpoint,
						endSessionEndpoint,
						restApiTokenEndpoint,
						restApiPublicEndpoint,
						issuer,
						walletChains,
						defaultScopes + optionalScopes,
						identityProviders)
	}
}
