using System.Net.Http;

namespace HyperId.Private
{
    internal class MFARequest : RestApiRequest
    {
        public MFARequest(int transactionId,
            IHyperIDSDKAuthRestApi api,
            string uriPath,
            HttpContent content) : base(api, uriPath, content)
        {
            TransactionId = transactionId;
        }

        public int TransactionId { get; set; }
    }
}
