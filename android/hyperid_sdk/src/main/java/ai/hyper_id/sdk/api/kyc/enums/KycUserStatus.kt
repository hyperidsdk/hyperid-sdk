package ai.hyper_id.sdk.api.kyc.enums

enum class KycUserStatus
{
	NONE,
	PENDING,
	COMPLETE_SUCCESS,
	COMPLETE_FAIL_RETRAYABLE,
	COMPLETE_FAIL_FINAL,
	DELETED
}
