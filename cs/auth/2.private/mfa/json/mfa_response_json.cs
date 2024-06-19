using System;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace HyperId.Private
{
    /// <summary>
    /// 
    /// </summary>
    internal class AvailabilityCheckResponseJson
    {
        [JsonPropertyName("result")]
        public int Result { get; set; }


        [JsonPropertyName("is_available")]
        public bool IsAvailable { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TransactionStartResponseJson
    {
        [JsonPropertyName("result")]
        public int Result { get; set; }


        [JsonPropertyName("transaction_id")]
        public int TransactionId { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TransactionCancelResponseJson
    {
        [JsonPropertyName("result")]
        public int Result { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TransactionStatusCheckResponseJson
    {
        [JsonPropertyName("result")]
        public int Result { get; set; }


        [JsonPropertyName("transaction_id")]
        public int TransactionId { get; set; }


        [JsonPropertyName("transaction_status")]
        public int TransactionStatus { get; set; }


        [JsonPropertyName("transaction_complete_result")]
        public int TransactionCompleteResult { get; set; }
    }
}
