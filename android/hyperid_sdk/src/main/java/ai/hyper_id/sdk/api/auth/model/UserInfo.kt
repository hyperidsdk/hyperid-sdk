package ai.hyper_id.sdk.api.auth.model

//**************************************************************************************************
//	WalletInfo
//--------------------------------------------------------------------------------------------------
data class WalletInfo(val walletAddress		: String?		= null,
					  val walletChainId		: String?		= null,
					  val walletSourceId	: Int?			= null,
					  val isWalletVerified	: Boolean		= false,
					  val walletFamilyId	: Int?			= null,
					  val walletTags		: String?		= null)

//**************************************************************************************************
//	UserInfo
//--------------------------------------------------------------------------------------------------
data class UserInfo(val userId						: String?			= null,
					val isGuest						: Boolean			= false,
					val email						: String?			= null,
					val isEmailVerified				: Boolean			= false,
					val deviceId					: String?			= null,
					val ip							: String?			= null,
					val walletInfo					: WalletInfo?		= null)
