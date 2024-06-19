using System.Text.Json.Serialization;

namespace HyperId.Private
{
    internal class TransactionInfo
    {
        public TransactionInfo(string question)
        {
            Type = "question";
            Question = question;
        }

        [JsonPropertyName("type")]
        public string Type {  get; set; }

        [JsonPropertyName("action_info")]
        public string Question { get; set; }
    }

    internal class TransactionStartValues
    {
        public TransactionStartValues(string question)
        {
            Version = 1;
            info = new TransactionInfo(question);
        }

        [JsonPropertyName("version")]
        public int Version { get; set; }

        [JsonPropertyName("action")]
        public TransactionInfo info { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TransactionStartRequestJson
    {
        public TransactionStartRequestJson(string values,
            string code)
        {
            TemplateId = 4; //hardcoded APPROVE template 
            Values = values;
            Code = code;
        }

        [JsonPropertyName("template_id")]
        public int TemplateId { get; set; }     

        [JsonPropertyName("values")]
        public string Values { get; set; }

        [JsonPropertyName("code")]
        public string Code { get; set; }
    }

    /// <summary>
    /// 
    /// </summary>
    internal class TransactionIdRequestJson
    {
        public TransactionIdRequestJson(int transactionId)
        {
            TransactionId = transactionId;
        }

        [JsonPropertyName("transaction_id")]
        public int TransactionId { get; set; }
    }

}//namespace HyperId.Private
