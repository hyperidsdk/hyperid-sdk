using Microsoft.EntityFrameworkCore.Metadata;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;


namespace HyperId.SDK.Authorization
{
    public struct TransactionData
    {
        public TransactionData(string addressTo,
                               string chainId,
                               string? addressFrom,
                               string? value,
                               string? data,
                               string? gas,
                               string? nonce)
        {
            To = addressTo;
            Chain = chainId;
            From = addressFrom;
            Value = value;
            Data = data;
            Gas = gas;
            Nonce = nonce;
        }

        public bool IsValid() {
            string hexPattern = @"(0[xX][0-9a-fA-F]+)\b";
            string numberPattern = @"\d+(\.\d+)?";

            if(Regex.IsMatch(To, hexPattern)) return false;
            if (!Value && !Data) return false;
            if (From && !Regex.IsMatch(From, hexPattern)) return false;
            if (Value && !Regex.IsMatch(Value, numberPattern) && !Regex.IsMatch(Value, hexPattern)) return false;
            if (Data && !Regex.IsMatch(Data, hexPattern)) return false;

            return true;
        }

        [JsonPropertyName("to")]
        public string To { get; set; }
        
        [JsonPropertyName("chain")]
        public string Chain { get; set; }

        [JsonPropertyName("from")]
        public string? From { get; set; }

        [JsonPropertyName("value")]
        public string? Value { get; set; }

        [JsonPropertyName("data")]
        public string? Data { get; set; }

        [JsonPropertyName("gas")]
        public string? Gas { get; set; }

        [JsonPropertyName("nonce")]
        public string? Nonce { get; set; }
    }
}//HyperId.SDK.Authorization