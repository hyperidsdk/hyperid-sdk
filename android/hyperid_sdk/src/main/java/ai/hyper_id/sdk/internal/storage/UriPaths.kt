package ai.hyper_id.sdk.internal.storage

internal object UriPaths
{
	internal const val pathDataSetByEmail			= "user-data/by-email/set"
	internal const val pathDataGetByEmail			= "user-data/by-email/get"
	internal const val pathKeysGetByEmail			= "user-data/by-email/list-get"
	internal const val pathKeysGetSharedByEmail		= "user-data/by-email/shared-list-get"
	internal const val pathDataDeleteByEmail		= "user-data/by-email/delete"

	internal const val pathDataSetByUserId			= "user-data/by-user-id/set"
	internal const val pathDataGetByUserId			= "user-data/by-user-id/get"
	internal const val pathKeysGetByUserId			= "user-data/by-user-id/list-get"
	internal const val pathKeysGetSharedByUserId	= "user-data/by-user-id/shared-list-get"
	internal const val pathDataDeleteByUserId		= "user-data/by-user-id/delete"

	internal const val pathDataSetByWalletId		= "user-data/by-wallet/set"
	internal const val pathDataGetByWalletId		= "user-data/by-wallet/get"
	internal const val pathKeysGetByWalletId		= "user-data/by-wallet/list-get"
	internal const val pathKeysSharedGetByWalletId	= "user-data/by-wallet/shared-list-get"
	internal const val pathDataDeleteByWalletId		= "user-data/by-wallet/delete"

	internal const val pathWalletGet				= "user-wallets/get"

	internal const val pathDataSetByIdP				= "user-data/by-idp/set"
	internal const val pathDataGetByIdP				= "user-data/by-idp/get"
	internal const val pathKeysGetByIdP				= "user-data/by-idp/list-get"
	internal const val pathKeysSharedGetByIdP		= "user-data/by-idp/shared-list-get"
	internal const val pathDataDeleteByIdP			= "user-data/by-idp/delete"
}
