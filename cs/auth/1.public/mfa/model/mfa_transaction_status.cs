namespace HyperId.SDK.MFA
{
    public class MFATransactionStatus
    {
        public MFATransactionStatus(int transactionId,
            MfaTransactionStatusGetResult statusGetResult,
            MfaTransactionStatus? status,
            MfaTransactionCompleteResult? completeResult)
        {
            TransactionId = transactionId;
            StatusGetResult = statusGetResult;
            Status = status;
            CompleteResult = completeResult;
        }

        public int TransactionId {  get; set; }

        public MfaTransactionStatusGetResult StatusGetResult { get; set; }

        public MfaTransactionStatus? Status {  get; set; }

        public MfaTransactionCompleteResult? CompleteResult { get; set; }
    }
}//namespace HyperId.SDK.MFA
