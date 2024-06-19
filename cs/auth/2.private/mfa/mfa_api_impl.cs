using HyperId.SDK;
using HyperId.SDK.MFA;
using System;
using System.Diagnostics.CodeAnalysis;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.Private
{
    internal class MFASDKImpl : IHyperIDSDKMFA
    {
        private IHyperIDSDKAuthRestApi authApi;
        private bool isAvailable  = false;

        public MFASDKImpl(IHyperIDSDKAuthRestApi auth)
        {
            this.authApi = auth;
        }

        async Task<bool> IHyperIDSDKMFA.AvailabilityCheckAsync(CancellationToken cancellationToken)
        {
            HttpContent content = new StringContent("");
            RestApiRequest request = new RestApiRequest(authApi,
                "mfa-client/availability-check",
                content);

            return await AvailabilityCheckAsync(request, cancellationToken);
        }
        private async Task<bool> AvailabilityCheckAsync(RestApiRequest request,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            AvailabilityCheckResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<AvailabilityCheckResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if(jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -4 || jsonResponse.Result == -5) // access token 
            {
                return await AvailabilityCheckAsync(request, cancellationToken);
            }
            else if(jsonResponse.Result == -2 || jsonResponse.Result == -1)     //-2 invalid param, -1 - service is temporarily unavailable
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
            else if (jsonResponse.Result == 0)      //success
            {
                isAvailable = jsonResponse.IsAvailable;
            }
            else
            {
                isAvailable = false;
            }
            return isAvailable;
        }

        async Task<int> IHyperIDSDKMFA.TransactionStartAsync([NotNull] string question,
            [NotNull] string code,
            CancellationToken cancellationToken)
        {
            TransactionStartValues valuesClass = new TransactionStartValues(question);
            string jsonValues = JsonSerializer.Serialize(valuesClass);

            TransactionStartRequestJson contentClass = new TransactionStartRequestJson(jsonValues, code);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(authApi,
                "mfa-client/transaction/start/v2",
                content);

            return await TransactionStartAsync(request, cancellationToken);
        }
        private async Task<int> TransactionStartAsync(RestApiRequest request,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            TransactionStartResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<TransactionStartResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -4 || jsonResponse.Result == -5) // access token 
            {
                return await TransactionStartAsync(request, cancellationToken);
            }
            else if (jsonResponse.Result == -2 || jsonResponse.Result == -1 || jsonResponse.Result == -8)     //-2 invalid param, -1 - service is temporarily unavailable, -8 - unsupported template id
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
            else if(jsonResponse.Result == -7)      //Failure due to the user's device with HyperID Authenticator App not being found
            {
                throw new HyperIDSDKExceptionHyperIDAuthenticatorNotFound();
            }
            else if (jsonResponse.Result == 0)      //success
            {
                return jsonResponse.TransactionId;
            }
            return -1;
        }

        async Task<bool> IHyperIDSDKMFA.TransactionCancelAsync(int transactionId,
            CancellationToken cancellationToken)
        {
            string jsonContent = JsonSerializer.Serialize(new TransactionIdRequestJson(transactionId));
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(authApi,
                "mfa-client/transaction/cancel",
                content);

            return await TransactionCancelAsync(request, cancellationToken);
        }
        private async Task<bool> TransactionCancelAsync(RestApiRequest request,
           CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            TransactionCancelResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<TransactionCancelResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -4 || jsonResponse.Result == -5) // access token 
            {
                return await TransactionCancelAsync(request, cancellationToken);
            }
            else if (jsonResponse.Result == -2          //invalid param, 
                || jsonResponse.Result == -1)           //service is temporarily unavailable
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
            else if (jsonResponse.Result == -7)      //Failure due to the user's device with HyperID Authenticator App not being found
            {
                throw new HyperIDSDKExceptionHyperIDAuthenticatorNotFound();
            }
            else if (jsonResponse.Result == 0       //success
                || jsonResponse.Result == -6        //Failure  not found
                || jsonResponse.Result == -8        //Failure  already been expired
                || jsonResponse.Result == -9        //Failure  already been completed
                || jsonResponse.Result == -10)      //Failure  already been canceled
            {
                return true;
            }
            return false;
        }

        async Task<MFATransactionStatus> IHyperIDSDKMFA.TransactionStatusCheckAsync(int transactionId,
            CancellationToken cancellationToken)
        {
            string jsonContent = JsonSerializer.Serialize(new TransactionIdRequestJson(transactionId));
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            MFARequest request = new MFARequest(transactionId,
                authApi,
                "mfa-client/transaction/status-get",
                content);

            return await TransactionStatusCheckAsync(request, cancellationToken);
        }
        private async Task<MFATransactionStatus> TransactionStatusCheckAsync(MFARequest request,
           CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await request.StartAsync(cancellationToken);
            TransactionStatusCheckResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<TransactionStatusCheckResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -4 || jsonResponse.Result == -5) // access token 
            {
                return await TransactionStatusCheckAsync(request, cancellationToken);
            }
            else if (jsonResponse.Result == -2          //invalid param, 
                || jsonResponse.Result == -1)           //service is temporarily unavailable
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
            else if (jsonResponse.Result == -6)       //Failure due to a transaction with the provided ID is not found
            {
                return new MFATransactionStatus(request.TransactionId,
                    MfaTransactionStatusGetResult.TRANSACTION_NOT_FOUND,
                    null,
                    null);
            }
            else if (jsonResponse.Result == 0)       //success
            {
                MfaTransactionStatus status = MfaTransactionStatus.EXPIRED;
                switch (jsonResponse.TransactionStatus)
                {
                    case 0: status = MfaTransactionStatus.PENDING; break;
                    case 1: status = MfaTransactionStatus.COMPLETED; break;
                    case 2: status = MfaTransactionStatus.EXPIRED; break;
                    case 4: status = MfaTransactionStatus.CANCELLED; break;
                }
                MfaTransactionCompleteResult? completeResult = null;
                switch(jsonResponse.TransactionCompleteResult)
                {
                    case 0: completeResult = MfaTransactionCompleteResult.APPROVED; break;
                    case 1: completeResult = MfaTransactionCompleteResult.DENIED; break;
                }
                return new MFATransactionStatus(request.TransactionId,
                    MfaTransactionStatusGetResult.SUCCESS,
                    status,
                    completeResult);
            }
            return new MFATransactionStatus(request.TransactionId,
                    MfaTransactionStatusGetResult.TRANSACTION_NOT_FOUND,
                    null,
                    null);
        }
    }
}