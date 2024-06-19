using HyperId.SDK.Authorization;
using System.Collections.Generic;

namespace HyperId.SDK.KYC
{
    public class KycUserStatusResponse
    {
        public KycUserStatusResponse(KycUserStatusGetResult result,
            KycVerificationLevel verificationLevel,
            KycUserStatus userStatus,
            string? kycId,
            string? firstName,
            string? lastName,
            string? birthday,
            string? countryA2,
            string? countryA3,
            string? providedCountryA2,
            string? providedCountryA3,
            string? addressCountryA2,
            string? addressCountryA3,
            string? phoneNumberCountryA2,
            string? phoneNumberCountryA3,
            string? phoneNumberCountryCode,
            List<string> ipCountriesA2,
            List<string> ipCountriesA3,
            string? moderationComment,
            List<string> rejectReasons,
            string? supportLink,
            long createDt,
            long reviewCreateDt,
            long reviewCompleteDt,
            long expirationDt)
        {
            Result = result;
            VerificationLevel = verificationLevel;
            UserStatus = userStatus;
            KycId = kycId;
            FirstName = firstName;
            LastName = lastName;
            Birthday = birthday;
            CountryA2 = countryA2;
            CountryA3 = countryA3;
            ProvidedCountryA2 = providedCountryA2;
            ProvidedCountryA3 = providedCountryA3;
            AddressCountryA2 = addressCountryA2;
            AddressCountryA3 = addressCountryA3;
            PhoneNumberCountryA2 = phoneNumberCountryA2;
            PhoneNumberCountryA3 = phoneNumberCountryA3;
            PhoneNumberCountryCode = phoneNumberCountryCode;
            IpCountriesA2 = ipCountriesA2;
            IpCountriesA3 = ipCountriesA3;
            ModerationComment = moderationComment;
            RejectReasons = rejectReasons;
            SupportLink = supportLink;
            CreateDt = createDt;
            ReviewCreateDt = reviewCreateDt;
            ReviewCompleteDt = reviewCompleteDt;
            ExpirationDt = expirationDt;
        }
        public KycUserStatusGetResult Result { get; set; }

        public KycVerificationLevel VerificationLevel { get; set; }

        public KycUserStatus UserStatus { get; set; }

        public string? KycId { get; set; }

        public string? FirstName { get; set; }

        public string? LastName { get; set; }

        public string? Birthday { get; set; }

        public string? CountryA2 { get; set; }

        public string? CountryA3 { get; set; }

        public string? ProvidedCountryA2 { get; set; }

        public string? ProvidedCountryA3 { get; set; }

        public string? AddressCountryA2 { get; set; }

        public string? AddressCountryA3 { get; set; }

        public string? PhoneNumberCountryA2 { get; set; }

        public string? PhoneNumberCountryA3 { get; set; }

        public string? PhoneNumberCountryCode { get; set; }

        public List<string> IpCountriesA2 { get; set; }

        public List<string> IpCountriesA3 { get; set; }

        public string? ModerationComment { get; set; }

        public List<string> RejectReasons { get; set; }

        public string? SupportLink { get; set; }

        public long CreateDt { get; set; }

        public long ReviewCreateDt { get; set; }

        public long ReviewCompleteDt { get; set; }

        public long ExpirationDt { get; set; }
    }
}//namespace HyperId.SDK.KYC
