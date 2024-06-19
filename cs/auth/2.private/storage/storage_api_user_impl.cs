using HyperId.Private;
using Microsoft.IdentityModel.Tokens;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    internal class StorageApiUserImpl : IStorageApiUser
    {
        private IHyperIDSDKAuthRestApi RestApi { get; set; }
        private const int PAGE_SIZE = 100;

        public StorageApiUserImpl(IHyperIDSDKAuthRestApi restApi)
        {
            RestApi = restApi;
        }


        #region Data Set
        async Task<DataSetResult> IStorageApiUser.DataSetAsync([NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken)
        {
            UserDataSetJson contentClass = new UserDataSetJson(key,
                value,
                EnumHelper.UserDataAccessScopeToValue(accessScope));
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-user-id/set",
                content);

            return await DataSetAsync(request, cancellationToken);
        }
        private async Task<DataSetResult> DataSetAsync(RestApiRequest restApiRequest,
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
            else if (jsonResponse.Result == -6)      //fail_by_key_access_denied
            {
                return DataSetResult.FAIL_BY_KEY_ACCESS_DENIED;
            }
            else if (jsonResponse.Result == -7)     //fail_by_key_invalid
            {
                return DataSetResult.FAIL_BY_KEY_INVALID;
            }
            else if (jsonResponse.Result == 0)      //success
            {
                return DataSetResult.SUCCESS;
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
        async Task<DataGetResult> IStorageApiUser.DataGetAsync(string key,
            CancellationToken cancellationToken)
        {
            List<string> keysList = new List<string>();
            keysList.Add(key);
            KeysJson contentClass = new KeysJson(keysList);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-user-id/get",
                content);

            return await DataGetAsync(request, cancellationToken);
        }
        private async Task<DataGetResult> DataGetAsync(RestApiRequest restApiRequest,
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
            else if (jsonResponse.Result == -6)      //fail_by_keys_size_limit_reached
            {
                return new DataGetResult(DataGetRequestResult.FAIL_BY_TOO_MANY_KEYS_IN_REQUEST,
                    null,
                    null);
            }
            else if (jsonResponse.Result == 0)      //success
            {
                if (!jsonResponse.Values.IsNullOrEmpty())
                {
                    return new DataGetResult(DataGetRequestResult.SUCCESS,
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
                return new DataGetResult(DataGetRequestResult.FAIL_BY_KEYS_NOT_FOUND,
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
        async Task<KeysGetResult> IStorageApiUser.KeysGetAsync(CancellationToken cancellationToken)
        {
            KeysGetJson contentClass = new KeysGetJson(1);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequest request = new RestApiRequest(RestApi,
                "user-data/by-user-id/list-get",
                content);

            return await KeysGetAsync(request, cancellationToken);
        }
        async private Task<KeysGetResult> KeysGetAsync(RestApiRequest restApiRequest, CancellationToken cancellationToken)
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
                return new KeysGetResult
                {
                    KeysPublic = jsonResponse.KeysPublic ?? new List<string>(),
                    KeysPrivate = jsonResponse.KeysPrivate ?? new List<string>()
                };
            }
            else if (jsonResponse.Result == 1)    //success_not_found
            {
                return new KeysGetResult
                {
                    KeysPublic = new List<string>(),
                    KeysPrivate = new List<string>()
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

        #region Keys Get Shared
        async Task<KeysSharedGetResult> IStorageApiUser.KeysGetSharedAsync(CancellationToken cancellationToken)
        {
            KeysSharedGetJson contentClass = new KeysSharedGetJson(PAGE_SIZE);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-user-id/shared-list-get",
                content);

            return await KeysGetSharedAsync(request, cancellationToken);
        }
        private async Task<KeysSharedGetResult> KeysGetSharedAsync(RestApiRequestKeysSharedGet restApiRequest,
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

                    return new KeysSharedGetResult { KeysShared = restApiRequest.KeysShared() };
                }
                else
                {
                    restApiRequest.KeysAdd(jsonResponse.KeysShared);

                    return await KeysGetSharedAsync(restApiRequest, cancellationToken);
                }
            }
            else if (jsonResponse.Result == 1)      //success_not_found
            {
                return new KeysSharedGetResult { KeysShared = new List<string>() };
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
        async Task<bool> IStorageApiUser.DataDeleteAsync(List<string> keys, CancellationToken cancellationToken)
        {
            KeysJson contentClass = new KeysJson(keys);
            string jsonContent = JsonSerializer.Serialize(contentClass);
            HttpContent content = new StringContent(jsonContent,
                Encoding.UTF8,
                "application/json");
            RestApiRequestKeysSharedGet request = new RestApiRequestKeysSharedGet(RestApi,
                "user-data/by-user-id/delete",
                content);

            return await DataDeleteAsync(request, cancellationToken);
        }

        async private Task<bool> DataDeleteAsync(RestApiRequestKeysSharedGet restApiRequest, CancellationToken cancellationToken)
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
                return true;
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
