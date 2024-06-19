using System;
using System.Net.Http.Json;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using HyperId.SDK.KYC;
using HyperId.SDK.Authorization;
using HyperId.SDK;

namespace HyperId.Private
{
    internal class KycSDKImpl : IHyperIDSDKKyc
    {
        private IHyperIDSDKAuthRestApi authApi;
        private int IdGenerator { get; set; } = 0;

        

        public KycSDKImpl(IHyperIDSDKAuthRestApi auth)
        {
            this.authApi = auth;
        }

        public async Task<KycUserStatusResponse> UserStatusGetAsync(KycVerificationLevel kycVerificationLevel,
            CancellationToken cancellationToken)
        {
            int verificationLevelValue = 3;
            switch (kycVerificationLevel)
            {
                case KycVerificationLevel.BASIC: verificationLevelValue = 3; break;
                case KycVerificationLevel.FULL: verificationLevelValue = 4; break;
            }

            string jsonContent = JsonSerializer.Serialize(new StatusGetRequestJson(++IdGenerator, verificationLevelValue));
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(authApi,
                "kyc/user/status-get",
                content);
            return await UserStatusGetAsync(request, cancellationToken);
        }
        private async Task<KycUserStatusResponse> UserStatusGetAsync(RestApiRequest request,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            StatusGetResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<StatusGetResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -1 || jsonResponse.Result == -2 || jsonResponse.Result == -3) // access token 
            {
                return await UserStatusGetAsync(request, cancellationToken);
            }
            else if (jsonResponse.Result == -5      //invalid param
                || jsonResponse.Result == -4        //service is temporarily unavailable
                || jsonResponse.Result == -6)       //by billing
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
            else if (jsonResponse.Result == 0               //success
                || jsonResponse.Result == -7                //fail by user not found
                || jsonResponse.Result == -8)               //fail by user kyc deleted
            {
                return jsonResponse.ToUserStatus();
            }
            throw new HyperIDSDKException("Unknown error");
        }

        public async Task<KycUserStatusTopLevelResponse> UserStatusTopLevelGetAsync(CancellationToken cancellationToken)
        {
            string jsonContent = JsonSerializer.Serialize(new TopLevelStatusGetRequestJson(++IdGenerator));
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(authApi,
                "kyc/user/status-get",
                content);
            return await UserStatusTopLevelGetAsync(request, cancellationToken);
        }
        private async Task<KycUserStatusTopLevelResponse> UserStatusTopLevelGetAsync(RestApiRequest request,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            TopLevelStatusGetResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<TopLevelStatusGetResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -1
                || jsonResponse.Result == -2
                || jsonResponse.Result == -3) // access token 
            {
                return await UserStatusTopLevelGetAsync(request, cancellationToken);
            }
            else if (jsonResponse.Result == 0               //success
                || jsonResponse.Result == -6)               //fail by user not found
            {
                return jsonResponse.ToTopLevelUserStatus();
            }
            else
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
    }
}
