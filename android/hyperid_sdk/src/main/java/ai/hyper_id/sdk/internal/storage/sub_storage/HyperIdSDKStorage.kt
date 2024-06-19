package ai.hyper_id.sdk.internal.storage.sub_storage

import ai.hyper_id.sdk.internal.auth.rest_api.IRestApiInterface
import kotlinx.serialization.json.Json

internal open class HyperIdSDKStorage(protected val restApi : IRestApiInterface)
{
	protected val jsonParser = Json { ignoreUnknownKeys = true }
}
