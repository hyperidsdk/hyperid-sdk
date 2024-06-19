using HyperId.Private;
using Microsoft.IdentityModel.Tokens;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Net.Http;
using System.Text.Json;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net.Http.Json;

namespace HyperId.SDK.Storage
{
    internal class StorageApiWalletImpl : IStorageApiWallet
    {
        private IHyperIDSDKAuthRestApi RestApi { get; set; }
        private const int PAGE_SIZE = 100;

        public StorageApiWalletImpl(IHyperIDSDKAuthRestApi restApi)
        {
            RestApi = restApi;
        }

        #region Data Set
        async Task<DataSetByWalletResult> IStorageApiWallet.DataSetAsync([NotNull] string walletAddress, 
            [NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken)
        {
            UserDataSetWalletJson contentClass = new()
            {
                WalletAddress = walletAddress,
                Key = key,
                Value = value,
                AccessScope = EnumHelper.UserDataAccessScopeToValue(accessScope)
            };
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-wallet/set",
                content);

            return await DataSetAsync(request, cancellationToken);
        }
        private async Task<DataSetByWalletResult> DataSetAsync(RestApiRequest restApiRequest,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await restApiRequest.StartAsync(cancellationToken);
            SimpleResponceJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<SimpleResponceJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -2 || jsonResponse.Result == -1) // access token 
            {
                return await DataSetAsync(restApiRequest, cancellationToken);
            }
            else if (jsonResponse.Result == 0)      //success
            {
                return DataSetByWalletResult.SUCCESS;
            }
            else if (jsonResponse.Result == -6)      //fail_by_wallet_not_exists
            {
                return DataSetByWalletResult.FAIL_WALLET_NOT_EXIST;
            }
            else if (jsonResponse.Result == -7)      //fail_by_key_access_denied
            {
                return DataSetByWalletResult.FAIL_BY_KEY_ACCESS_DENIED;
            }
            else if (jsonResponse.Result == -8)     //fail_by_key_invalid
            {
                return DataSetByWalletResult.FAIL_BY_KEY_INVALID;
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters

                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        #endregion

        #region Data Get
        async Task<DataGetByWalletResult> IStorageApiWallet.DataGetAsync([NotNull] string walletAddress,
            [NotNull] string key,
            CancellationToken cancellationToken)
        {
            List<string> keysList = new() { key };
            KeysWithWalletJson contentClass = new KeysWithWalletJson(walletAddress, keysList);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-wallet/get",
                content);

            return await DataGetAsync(request, cancellationToken);
        }
        private async Task<DataGetByWalletResult> DataGetAsync(RestApiRequest restApiRequest,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await restApiRequest.StartAsync(cancellationToken);
            UserDataGetResultJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<UserDataGetResultJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -2 || jsonResponse.Result == -1) // access token 
            {
                return await DataGetAsync(restApiRequest, cancellationToken);
            }
            else if (jsonResponse.Result == 0)      //success
            {
                if (!jsonResponse.Values.IsNullOrEmpty())
                {
                    return new DataGetByWalletResult(DataGetByWalletRequestResult.SUCCESS,
                        jsonResponse.Values?.FirstOrDefault()?.Key!,
                        jsonResponse.Values?.FirstOrDefault()?.Value);
                }
                else
                {
                    throw new HyperIDSDKExceptionUnderMaintenace();
                }
            }
            else if (jsonResponse.Result == 1)    //success_not_found
            {
                return new DataGetByWalletResult(DataGetByWalletRequestResult.FAIL_BY_KEYS_NOT_FOUND,
                    null,
                    null);
            }
            else if (jsonResponse.Result == -7)      //fail_by_keys_size_limit_reached
            {
                return new DataGetByWalletResult(DataGetByWalletRequestResult.FAIL_BY_TOO_MANY_KEYS_IN_REQUEST,
                    null,
                    null);
            }
            else if(jsonResponse.Result == -6)
            {
                return new DataGetByWalletResult(DataGetByWalletRequestResult.FAIL_BY_WALLET_NOT_FOUND,
                    null,
                    null);
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        #endregion

        #region Keys Get
        async Task<KeysGetByWalletResult> IStorageApiWallet.KeysGetAsync([NotNull] string walletAddress,
            CancellationToken cancellationToken)
        {
            KeysGetByWalletJson contentClass = new KeysGetByWalletJson(walletAddress);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-wallet/list-get",
                content);

            return await KeysGetAsync(request, cancellationToken);
        }
        async private Task<KeysGetByWalletResult> KeysGetAsync(RestApiRequest restApiRequest, CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await restApiRequest.StartAsync(cancellationToken);
            KeysGetResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<KeysGetResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -2 || jsonResponse.Result == -1) // access token 
            {
                return await KeysGetAsync(restApiRequest, cancellationToken);
            }
            else if (jsonResponse.Result == 0)      //success
            {
                return new KeysGetByWalletResult
                {
                    Result = KeysGetByWalletRequestResult.SUCCESS,
                    KeysPublic = jsonResponse.KeysPublic ?? new List<string>(),
                    KeysPrivate = jsonResponse.KeysPrivate ?? new List<string>()
                };
            }
            else if (jsonResponse.Result == 1)    //success_not_found
            {
                return new KeysGetByWalletResult { Result = KeysGetByWalletRequestResult.SUCCESS };
            }
            else if (jsonResponse.Result == -6)      //fail_by_wallet_not_exists
            {
                return new KeysGetByWalletResult { Result = KeysGetByWalletRequestResult.FAIL_BY_WALLET_NOT_FOUND };
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        #endregion

        #region Keys Get Shared
        async Task<KeysSharedGetByWalletResult> IStorageApiWallet.KeysGetSharedAsync([NotNull] string walletAddress,
            CancellationToken cancellationToken)
        {
            KeysSharedGetByWalletJson contentClass = new KeysSharedGetByWalletJson(walletAddress, PAGE_SIZE);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-wallet/shared-list-get",
                content);

            return await KeysGetSharedAsync(request, cancellationToken);
        }
        private async Task<KeysSharedGetByWalletResult> KeysGetSharedAsync(RestApiRequestKeysSharedGet restApiRequest,
            CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await restApiRequest.StartAsync(cancellationToken);
            KeysSharedGetResponseJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<KeysSharedGetResponseJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -2 || jsonResponse.Result == -1) // access token 
            {
                return await KeysGetSharedAsync(restApiRequest, cancellationToken);
            }
            else if (jsonResponse.Result == 0)      //success
            {
                if (jsonResponse.KeysShared == null
                    || jsonResponse.KeysShared.Count < PAGE_SIZE
                    || jsonResponse.NextSearchId == null)
                {
                    if (jsonResponse.KeysShared != null)
                    {
                        restApiRequest.KeysAdd(jsonResponse.KeysShared);
                    }

                    return new KeysSharedGetByWalletResult
                        {
                            Result = KeysGetByWalletRequestResult.SUCCESS,
                            KeysShared = restApiRequest.KeysShared()
                        };
                }
                else
                {
                    restApiRequest.KeysAdd(jsonResponse.KeysShared);

                    return await KeysGetSharedAsync(restApiRequest, cancellationToken);
                }
            }
            else if (jsonResponse.Result == -6)      //fail_by_wallet_not_exists
            {
                return new KeysSharedGetByWalletResult
                    {
                        Result = KeysGetByWalletRequestResult.SUCCESS,
                        KeysShared = new List<string>()
                    };
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        #endregion

        #region Data Delete
        async Task<DataDeleteByWalletRequestResult> IStorageApiWallet.DataDeleteAsync([NotNull] string walletAddress,
            List<string> keys,
            CancellationToken cancellationToken)
        {
            KeysWithWalletJson contentClass = new KeysWithWalletJson(walletAddress, keys);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-wallet/delete",
                content);

            return await DataDeleteAsync(request, cancellationToken);
        }

        async private Task<DataDeleteByWalletRequestResult> DataDeleteAsync(RestApiRequestKeysSharedGet restApiRequest, CancellationToken cancellationToken)
        {
            HttpResponseMessage response = await restApiRequest.StartAsync(cancellationToken);
            SimpleResponceJson? jsonResponse;
            try
            {
                jsonResponse = await response.Content.ReadFromJsonAsync<SimpleResponceJson>(options: null, cancellationToken);
            }
            catch (Exception)
            {
                throw new HyperIDSDKExceptionUnderMaintenace();
            }

            if (jsonResponse == null
                || jsonResponse.Result == -3 || jsonResponse.Result == -2 || jsonResponse.Result == -1) // access token 
            {
                return await DataDeleteAsync(restApiRequest, cancellationToken);
            }
            else if (jsonResponse.Result == 0       //success
                || jsonResponse.Result == 1)        //success_not_found
            {
                return DataDeleteByWalletRequestResult.SUCCESS;
            }
            else if (jsonResponse.Result == -6)     //fail_by_wallet_not_exists
            {
                return DataDeleteByWalletRequestResult.FAIL_BY_WALLET_NOT_FOUND;
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        #endregion
    }
}
