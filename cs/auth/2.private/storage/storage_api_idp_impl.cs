using HyperId.Private;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Net.Http;
using System.Text.Json;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net.Http.Json;
using System;
using Microsoft.IdentityModel.Tokens;
using System.Linq;

namespace HyperId.SDK.Storage
{
    internal class StorageApiIdpImpl : IStorageApiIdp
    {
        private IHyperIDSDKAuthRestApi RestApi { get; set; }
        private const int PAGE_SIZE = 100;

        public StorageApiIdpImpl(IHyperIDSDKAuthRestApi restApi)
        {
            RestApi = restApi;
        }

        async Task<DataSetByIdentityProviderResult> IStorageApiIdp.DataSetAsync([NotNull] string identityProvider,
            [NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken = default)
        {
            UserDataSetIdpJson contentClass = new()
            {
                IdentityProvider = identityProvider,
                Key = key,
                Value = value,
                AccessScope = EnumHelper.UserDataAccessScopeToValue(accessScope)
            };
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-idp/set",
                content);

            return await DataSetAsync(request, cancellationToken);
        }
        private async Task<DataSetByIdentityProviderResult> DataSetAsync(RestApiRequest restApiRequest,
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
                return DataSetByIdentityProviderResult.SUCCESS;
            }
            else if (jsonResponse.Result == -6)      //fail_by_identity_provider_not_found
            {
                return DataSetByIdentityProviderResult.FAIL_IDENTITY_PROVIDER_NOT_EXIST;
            }
            else if (jsonResponse.Result == -7)      //fail_by_key_access_denied
            {
                return DataSetByIdentityProviderResult.FAIL_BY_KEY_ACCESS_DENIED;
            }
            else if (jsonResponse.Result == -8)     //fail_by_key_invalid
            {
                return DataSetByIdentityProviderResult.FAIL_BY_KEY_INVALID;
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters

                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
        async Task<DataGetByIdpResult> IStorageApiIdp.DataGetAsync([NotNull] string identityProvider,
            [NotNull] string key,
            CancellationToken cancellationToken = default)
        {
            List<string> keysList = new() { key };
            KeysWithIdpJson contentClass = new KeysWithIdpJson(identityProvider, keysList);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-idp/get",
                content);

            return await DataGetAsync(request, cancellationToken);
        }
        private async Task<DataGetByIdpResult> DataGetAsync(RestApiRequest restApiRequest,
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
                    return new DataGetByIdpResult(DataGetByIdentityProviderRequestResult.SUCCESS,
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
                return new DataGetByIdpResult(DataGetByIdentityProviderRequestResult.FAIL_BY_KEYS_NOT_FOUND,
                    null,
                    null);
            }
            else if (jsonResponse.Result == -7)      //fail_by_keys_size_limit_reached
            {
                return new DataGetByIdpResult(DataGetByIdentityProviderRequestResult.FAIL_BY_TOO_MANY_KEYS_IN_REQUEST,
                    null,
                    null);
            }
            else if (jsonResponse.Result == -6)     //fail_by_identity_provider_not_found
            {
                return new DataGetByIdpResult(DataGetByIdentityProviderRequestResult.FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND,
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

        async Task<KeysGetByIdpResult> IStorageApiIdp.KeysGetAsync([NotNull] string identityProvider,
            CancellationToken cancellationToken = default)
        {
            KeysGetByIdpJson contentClass = new KeysGetByIdpJson(identityProvider);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-idp/list-get",
                content);

            return await KeysGetAsync(request, cancellationToken);
        }
        private async Task<KeysGetByIdpResult> KeysGetAsync(RestApiRequest restApiRequest,
            CancellationToken cancellationToken)
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
                return new KeysGetByIdpResult
                {
                    Result = KeysGetByIdentityProviderRequestResult.SUCCESS,
                    KeysPublic = jsonResponse.KeysPublic ?? new List<string>(),
                    KeysPrivate = jsonResponse.KeysPrivate ?? new List<string>()
                };
            }
            else if (jsonResponse.Result == 1)    //success_not_found
            {
                return new KeysGetByIdpResult { Result = KeysGetByIdentityProviderRequestResult.SUCCESS };
            }
            else if (jsonResponse.Result == -6)      //fail_by_wallet_not_exists
            {
                return new KeysGetByIdpResult { Result = KeysGetByIdentityProviderRequestResult.FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND };
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }

        async Task<KeysSharedGetByIdpResult> IStorageApiIdp.KeysGetSharedAsync([NotNull] string identityProvider,
            CancellationToken cancellationToken = default)
        {
            KeysSharedGetByIdpJson contentClass = new KeysSharedGetByIdpJson(identityProvider, PAGE_SIZE);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-idp/shared-list-get",
                content);

            return await KeysGetSharedAsync(request, cancellationToken);
        }
        private async Task<KeysSharedGetByIdpResult> KeysGetSharedAsync(RestApiRequestKeysSharedGet restApiRequest,
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

                    return new KeysSharedGetByIdpResult
                    {
                        Result = KeysGetByIdentityProviderRequestResult.SUCCESS,
                        KeysShared = restApiRequest.KeysShared()
                    };
                }
                else
                {
                    restApiRequest.KeysAdd(jsonResponse.KeysShared);

                    return await KeysGetSharedAsync(restApiRequest, cancellationToken);
                }
            }
            else if (jsonResponse.Result == 1)      //success_not_found
            {
                return new KeysSharedGetByIdpResult
                {
                    Result = KeysGetByIdentityProviderRequestResult.SUCCESS,
                    KeysShared = restApiRequest.KeysShared()
                };
            }
            else if(jsonResponse.Result == -6)      //fail_by_identity_provider_not_found
            {
                return new KeysSharedGetByIdpResult
                {
                    Result = KeysGetByIdentityProviderRequestResult.FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND,
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


        async Task<DataDeleteByIdentityProviderRequestResult> IStorageApiIdp.DataDeleteAsync([NotNull] string identityProvider,
            List<string> keys,
            CancellationToken cancellationToken = default)
        {
            KeysWithIdpJson contentClass = new KeysWithIdpJson(identityProvider, keys);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-wallet/delete",
                content);

            return await DataDeleteAsync(request, cancellationToken);
        }
        async private Task<DataDeleteByIdentityProviderRequestResult> DataDeleteAsync(RestApiRequestKeysSharedGet restApiRequest, CancellationToken cancellationToken)
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
                return DataDeleteByIdentityProviderRequestResult.SUCCESS;
            }
            else if (jsonResponse.Result == -6)     //fail_by_wallet_not_exists
            {
                return DataDeleteByIdentityProviderRequestResult.FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND;
            }
            else
            {
                // -4 fail_by_service_temporary_not_valid
                // -5 fail_by_invalid_parameters
                throw new HyperIDSDKExceptionUnderMaintenace();
            }
        }
    }
}