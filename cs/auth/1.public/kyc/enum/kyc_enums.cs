
namespace HyperId.SDK.KYC
{
    public enum KycUserStatusGetResult
    {
        SUCCESS,
        FAIL_BY_USER_NOT_FOUND
    }

    public enum KycUserStatus
    {
        NONE,
        PENDING,
        COMPLETE_SUCCESS,
        COMPLETE_FAIL_RETRAYABLE,
        COMPLETE_FAIL_FINAL,
        DELETED
    }
}
