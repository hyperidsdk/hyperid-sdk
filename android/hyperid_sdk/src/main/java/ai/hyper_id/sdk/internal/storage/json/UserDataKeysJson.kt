package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class	UserDataKeysJson
					(
						@SerialName("value_keys")
						val keys				: List<String>
					)

@Serializable
internal data class	UserDataKeysByWalletJson
					(
						@SerialName("wallet_address")
						val walletAddress			: String,

						@SerialName("value_keys")
						val keys					: List<String>
					)

@Serializable
internal data class UserDataKeysIdentityProvider
					(
						@SerialName("identity_provider")
						val identityProvider		: String,

						@SerialName("value_keys")
						val keys					: List<String>
					)
