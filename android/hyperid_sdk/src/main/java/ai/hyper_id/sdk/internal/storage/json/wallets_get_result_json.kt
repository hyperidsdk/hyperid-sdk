package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WalletInfoJson
(
		@SerialName("address")
		@Required
		val address				: String,

		@SerialName("chain")
		@Required
		val chainId				: String,

		@SerialName("family")
		@Required
		val family				: Int,

		@SerialName("label")
		@Required
		val label				: String,

		@SerialName("tags")
		@Required
		val tags				: List<String>,
)

@Serializable
internal data class	WalletsGetResultJson
(
		@SerialName("result")
		@Required
		val result					: Int,

		@SerialName("wallets_private")
		@Required
		val walletsPrivate			: List<WalletInfoJson>,

		@SerialName("wallets_public")
		@Required
		val walletsPublic			: List<WalletInfoJson>
)
