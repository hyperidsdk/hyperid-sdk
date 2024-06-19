package ai.hyper_id.sdk.internal.auth.types

import ai.hyper_id.sdk.api.auth.model.ClientInfo
import ai.hyper_id.sdk.api.auth.model.KycVerificationLevel
import ai.hyper_id.sdk.api.auth.model.WalletFamily
import ai.hyper_id.sdk.api.auth.model.WalletGetMode
import ai.hyper_id.sdk.internal.auth.enums.AuthorizationFlowMode
import android.net.Uri

//**************************************************************************************************
//	uriBuilders
//--------------------------------------------------------------------------------------------------
internal object UriBuilders
{
	//==================================================================================================
	//	discover
	//--------------------------------------------------------------------------------------------------
	internal fun discover(_provider : Uri) : Uri
	{
		return Uri.withAppendedPath(_provider,
									"auth/realms/HyperID/.well-known/openid-configuration")
	}
	//==================================================================================================
	//	authorization
	//--------------------------------------------------------------------------------------------------
	internal fun authorization(_endpoint				: String,
							   _scopes					: List<String>,
							   _clientInfo				: ClientInfo,
							   _userId					: String?					= null,
							   _flowMode				: AuthorizationFlowMode?	= null,
							   _walletGetMode			: WalletGetMode?			= null,
							   _walletFamily			: WalletFamily?				= null,
							   _walletAddress			: String?					= null,
							   _verificationLevel		: KycVerificationLevel?		= null,
							   _identityProvider		: String?					= null,
							   _transactionInfo			: String?					= null)			: Uri
	{
		val endpoint	= Uri.parse(_endpoint)
		val uriBuilder	= Uri.Builder()
		uriBuilder.scheme(endpoint.scheme)
		uriBuilder.encodedAuthority(endpoint.encodedAuthority)
		uriBuilder.encodedPath(endpoint.encodedPath)
		uriBuilder.appendQueryParameter("response_type",				"code")
		uriBuilder.appendQueryParameter("scope",						_scopes.joinToString(" "))
		uriBuilder.appendQueryParameter("client_id",					_clientInfo.clientId)
		uriBuilder.appendQueryParameter("redirect_uri",					_clientInfo.redirectUri)

		if(_userId != null)
		{
			uriBuilder.appendQueryParameter("login_hint",				_userId)
			uriBuilder.appendQueryParameter("is_login_hint_required",	"1")
		}
		if(_flowMode != null)
		{
			uriBuilder.appendQueryParameter("flow_mode",				_flowMode.ToValue())
		}
		if(_walletGetMode != null)
		{
			uriBuilder.appendQueryParameter("wallet_get_mode",			when(_walletGetMode)
																		{
																			WalletGetMode.FAST			-> "2"
																			WalletGetMode.FULL			-> "3"
																		})
		}
		if(_verificationLevel != null)
		{
			uriBuilder.appendQueryParameter("verification_level",		when(_verificationLevel)
																		{
																			KycVerificationLevel.BASIC	-> "3"
																			KycVerificationLevel.FULL	-> "4"
																		})
		}
		if(_walletFamily != null)
		{
			uriBuilder.appendQueryParameter("wallet_family",			when(_walletFamily)
																		{
																			WalletFamily.ETHEREUM		-> "0"
																			WalletFamily.SOLANA			-> "1"
																		})
		}
		if(_walletAddress != null)
		{
			uriBuilder.appendQueryParameter("wallet_address",			_walletAddress)
		}
		if(_identityProvider != null)
		{
			uriBuilder.appendQueryParameter("identity_provider",		_identityProvider)
		}
		if(_transactionInfo != null)
		{
			uriBuilder.appendQueryParameter("transaction",				_transactionInfo)
		}
		return uriBuilder.build()
	}
}
