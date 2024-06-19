package ai.hyper_id.sdk.internal.auth.enums

//**************************************************************************************************
//	AuthorizationFlowMode
//--------------------------------------------------------------------------------------------------
internal enum class AuthorizationFlowMode
{
	NONE,
	SIGN_IN,
	SIGN_TO_SIGN_IN,
	WALLET_GET,
	GUEST_UPGRADE,
	IDENTITY_PROVIDER;

	//==================================================================================================
	//	ToValue
	//--------------------------------------------------------------------------------------------------
	fun ToValue()	: String
	{
		return	when(this)
				{
					NONE				-> "1"
					SIGN_IN				-> "0"
					SIGN_TO_SIGN_IN		-> "3"
					WALLET_GET			-> "4"
					GUEST_UPGRADE		-> "6"
					IDENTITY_PROVIDER	-> "9"
				}
	}
}
