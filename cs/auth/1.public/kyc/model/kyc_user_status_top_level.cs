using HyperId.SDK.Authorization;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace HyperId.SDK.KYC
{
    public class KycUserStatusTopLevelResponse
    {
        public KycUserStatusTopLevelResponse(KycUserStatusGetResult result, 
            KycVerificationLevel verificationLevel,
            KycUserStatus userStatus,
            long createDt,
            long reviewCreateDt,
            long reviewCompleteDt)
        {
            VerificationLevel = verificationLevel;
            UserStatus = userStatus;
            Result = result;
            CreateDt = createDt;
            ReviewCreateDt = reviewCreateDt;
            ReviewCompleteDt = reviewCompleteDt;
        }

        public KycUserStatusGetResult Result { get; set; }

        public KycVerificationLevel VerificationLevel { get; set; }

        public KycUserStatus UserStatus { get; set; }

        public long CreateDt { get; set; }

        public long ReviewCreateDt { get; set; }

        public long ReviewCompleteDt { get; set; }
    }
}//namespace HyperId.SDK.KYC
