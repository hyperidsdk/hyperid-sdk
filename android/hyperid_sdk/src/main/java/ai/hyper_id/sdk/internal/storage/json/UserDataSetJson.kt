package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDataSetJson
					(
						@SerialName("value_key")
						val key						: String,

						@SerialName("value_data")
						val value					: String,

						@SerialName("access_scope")
						val accessScope				: Int
					)

@Serializable
internal data class UserDataSetByWalletJson
					(
							@SerialName("wallet_address")
							val identityProvider		: String,

							@SerialName("value_key")
							val key						: String,

							@SerialName("value_data")
							val value					: String,

							@SerialName("access_scope")
							val accessScope				: Int
					)


@Serializable
internal data class UserDataSetByIdentityProviderJson
					(
							@SerialName("identity_provider")
							val identityProvider		: String,

							@SerialName("value_key")
							val key						: String,

							@SerialName("value_data")
							val value					: String,

							@SerialName("access_scope")
							val accessScope				: Int
					)
