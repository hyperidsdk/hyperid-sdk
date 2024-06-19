using HyperId.SDK.Authorization;
using HyperId.SDK.KYC;
using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace HyperId.Private
{
    internal class StatusGetResponseJson
    {
        public StatusGetResponseJson()
        {
            IpCountriesA2 = new List<string>();
            IpCountriesA3 = new List<string>();
            RejectReasons = new List<string>();
        }

        [JsonPropertyName("result")]
        public int Result {  get; set; }

        [JsonPropertyName("verification_level")]
        public int VerificationLevel {  get; set; }

        [JsonPropertyName("user_status")]
        public int UserStatus {  get; set; }

        [JsonPropertyName("kyc_id")]
        public string? KycId { get; set; }

        [JsonPropertyName("first_name")]
        public string? FirstName { get; set; }

        [JsonPropertyName("last_name")]
        public string? LastName { get; set; }

        [JsonPropertyName("birthday")]
        public string? Birthday { get; set; }

        [JsonPropertyName("country_a2")]
        public string? CountryA2 {  get; set; }

        [JsonPropertyName("country_a3")]
        public string? CountryA3 { get; set; }
    
        [JsonPropertyName("provided_country_a2")]
        public string? ProvidedCountryA2 { get; set; }
    
        [JsonPropertyName("provided_country_a3")]
        public string? ProvidedCountryA3 { get; set; }
    
        [JsonPropertyName("address_country_a2")]
        public string? AddressCountryA2 { get; set; }
    
        [JsonPropertyName("address_country_a3")]
        public string? AddressCountryA3 { get; set; }
    
        [JsonPropertyName("phone_number_country_a2")]
        public string? PhoneNumberCountryA2 { get; set; }
    
        [JsonPropertyName("phone_number_country_a3")]
        public string? PhoneNumberCountryA3 { get; set; }
    
        [JsonPropertyName("phone_number_country_code")]
        public string? PhoneNumberCountryCode { get; set; }
    
        [JsonPropertyName("ip_countries_a2")]
        public List<string> IpCountriesA2 { get; set; }
    
        [JsonPropertyName("ip_countries_a3")]
        public List<string> IpCountriesA3 { get; set; }
	    
        [JsonPropertyName("moderation_comment")]
        public string? ModerationComment { get; set; }

        [JsonPropertyName("reject_reasons")]
        public List<string> RejectReasons { get; set; }

        [JsonPropertyName("support_link")]
        public string? SupportLink { get; set; }

        [JsonPropertyName("create_dt")]
        public long CreateDt { get; set; }

        [JsonPropertyName("review_create_dt")]
        public long ReviewCreateDt { get; set; }

        [JsonPropertyName("review_complete_dt")]
        public long ReviewCompleteDt { get; set; }
        
        [JsonPropertyName("expiration_dt")]
        public long ExpirationDt { get; set; }

        public KycUserStatusResponse ToUserStatus()
        {
            KycUserStatusGetResult resultEnum = KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND;
            if(Result == 0)
            {
                resultEnum = KycUserStatusGetResult.SUCCESS;
            }

            KycVerificationLevel kycVerificationLevel = KycVerificationLevel.FULL;
            if(VerificationLevel == 3)
            {
                kycVerificationLevel = KycVerificationLevel.BASIC;
            }
            KycUserStatus kycUserStatus = KycUserStatus.NONE;
            switch(UserStatus)
            {
                case 0: kycUserStatus = KycUserStatus.NONE;                     break;
                case 1: kycUserStatus = KycUserStatus.PENDING;                  break;
                case 2: kycUserStatus = KycUserStatus.COMPLETE_SUCCESS;         break;
                case 3: kycUserStatus = KycUserStatus.COMPLETE_FAIL_RETRAYABLE; break;
                case 4: kycUserStatus = KycUserStatus.COMPLETE_FAIL_FINAL;      break;
                case 5: kycUserStatus = KycUserStatus.DELETED;                  break;
            }

            return new KycUserStatusResponse(resultEnum,
                kycVerificationLevel,
                kycUserStatus,
                KycId,
                FirstName,
                LastName,
                Birthday,
                CountryA2,
                CountryA3,
                ProvidedCountryA2,
                ProvidedCountryA3,
                AddressCountryA2,
                AddressCountryA3,
                PhoneNumberCountryA2,
                PhoneNumberCountryA3,
                PhoneNumberCountryCode,
                IpCountriesA2,
                IpCountriesA3,
                ModerationComment,
                RejectReasons,
                SupportLink,
                CreateDt,
                ReviewCreateDt,
                ReviewCompleteDt,
                ExpirationDt);
        }
    }

    internal class TopLevelStatusGetResponseJson
    {
        [JsonPropertyName("request_id")]
        public int RequestId { get; set; }

        [JsonPropertyName("verification_level")]
        public int VerificationLevelRaw { get; set; }

        [JsonPropertyName("user_status")]
        public int UserStatus { get; set; }

        [JsonPropertyName("result")]
        public int Result { get; set; }

        [JsonPropertyName("create_dt")]
        public long CreateDt { get; set; }

        [JsonPropertyName("review_create_dt")]
        public long ReviewCreateDt { get; set; }

        [JsonPropertyName("review_complete_dt")]
        public long ReviewCompleteDt { get; set; }

        public KycUserStatusTopLevelResponse ToTopLevelUserStatus()
        {
            KycUserStatusGetResult result = KycUserStatusGetResult.SUCCESS;
            if(result != 0)
            {
                result = KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND;
            }

            KycVerificationLevel kycVerificationLevel = KycVerificationLevel.FULL;
            if (VerificationLevelRaw == 3)
            {
                kycVerificationLevel = KycVerificationLevel.BASIC;
            }

            KycUserStatus kycUserStatus = KycUserStatus.NONE;
            switch (UserStatus)
            {
                case 0: kycUserStatus = KycUserStatus.NONE; break;
                case 1: kycUserStatus = KycUserStatus.PENDING; break;
                case 2: kycUserStatus = KycUserStatus.COMPLETE_SUCCESS; break;
                case 3: kycUserStatus = KycUserStatus.COMPLETE_FAIL_RETRAYABLE; break;
                case 4: kycUserStatus = KycUserStatus.COMPLETE_FAIL_FINAL; break;
                case 5: kycUserStatus = KycUserStatus.DELETED; break;
            }

            return new KycUserStatusTopLevelResponse(result, 
                kycVerificationLevel,
                kycUserStatus,
                CreateDt,
                ReviewCreateDt,
                ReviewCompleteDt)
            ;
        }
    }
}
