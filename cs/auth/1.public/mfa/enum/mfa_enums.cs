namespace HyperId.SDK.MFA
{
    public enum MfaTransactionStatusGetResult
    {
        SUCCESS,
        TRANSACTION_NOT_FOUND,
    }

    public enum MfaTransactionStatus
    {
        PENDING,
        COMPLETED,
        EXPIRED,
        CANCELLED
    }

    public enum MfaTransactionCompleteResult
    {
        APPROVED,
        DENIED
    }

}//namespace HyperId.SDK.MFA
