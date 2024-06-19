package ai.hyper_id.sdk.internal.storage.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal data class	UserDataKeysSharedGetJson
					(
						@SerialName("request_id")
						val requestId					: Int,

						@SerialName("search_id")
						val searchId					: String?	= null,

						@SerialName("page_size")
						val pageSize					: Int
					)

@Serializable
internal data class	UserDataKeysSharedGetByWalletJson
					(
						@SerialName("request_id")
						val requestId					: Int,

						@SerialName("wallet_address")
						val walletAddress				: String,

						@SerialName("search_id")
						val searchId					: String?	= null,

						@SerialName("page_size")
						val pageSize					: Int
					)

@Serializable
internal data class	UserDataKeysSharedGetByIdentityProviderJson
					(
						@SerialName("request_id")
						val requestId					: Int,

						@SerialName("identity_provider")
						val identityProvider			: String,

						@SerialName("search_id")
						val searchId					: String?	= null,

						@SerialName("page_size")
						val pageSize					: Int
					)

@Serializable
internal data class	UserDataKeysSharedGetResultJson
					(
						@SerialName("request_id")
						@Required
						val requestId					: Int,

						@SerialName("result")
						@Required
						val result						: Int,

						@SerialName("keys_shared")
						@Required
						val keysShared					: List<String>,

						@SerialName("next_search_id")
						@Required
						val nextSearchId				: String
					)
