using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace HyperId.Private
{
    /// <summary>
    /// 
    /// </summary>
    internal class StatusGetRequestJson
    {
        public StatusGetRequestJson(int requestId,
            int verificationLevel)
        {
            RequestId = requestId;
            VerificationLevel = verificationLevel;
        }

        [JsonPropertyName("request_id")]
        public int RequestId { get; set; }

        [JsonPropertyName("verification_level")]
        public int VerificationLevel { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TopLevelStatusGetRequestJson
    {
        public TopLevelStatusGetRequestJson(int requestId)
        {
            RequestId = requestId;
        }

        [JsonPropertyName("request_id")]
        public int RequestId { get; set; }
    }
}
