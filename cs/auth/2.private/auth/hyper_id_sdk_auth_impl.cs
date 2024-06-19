using Microsoft.IdentityModel.Tokens;
using System;
using System.Net.Http;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics.CodeAnalysis;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Net.Http.Json;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;
using System.Web;
using HyperId.SDK;
using HyperId.SDK.Authorization;
using System.Security.Cryptography;

namespace HyperId.Private
{
    /// <summary>
    /// class eCore
    /// </summary>
    internal class AuthSDKImpl : IHyperIDSDKAuth, IHyperIDSDKAuthRestApi
    {
        #region Private variables
        private string _providerInfo;
        private ClientInfo _clientInfo;
        private Discovery? _discover;
        private string? _accessToken;
        private string? _refreshToken;
        private RequestProcessor transport = new RequestProcessor();
        #endregion

        /// <summary>
        /// InitAsync
        /// </summary>
        public async Task InitAsync(
            [NotNull] ProviderInfo providerInfo,
            [NotNull] ClientInfo clientInfo,
            [AllowNull] string? refreshToken,
            [AllowNull] CancellationToken cancellationToken_)
        {
            await Task.Yield();

            ArgumentNullException.ThrowIfNull(providerInfo);
            ArgumentNullException.ThrowIfNull(clientInfo);

            switch(providerInfo)
            {
                case ProviderInfo.STAGE         : _providerInfo = "https://login-stage.hypersecureid.com";      break;
                case ProviderInfo.SANDBOX       : _providerInfo = "https://login-sandbox.hypersecureid.com";    break;
                case ProviderInfo.PRODUCTION    : _providerInfo = "https://login.hypersecureid.com";            break;
            }
            _clientInfo = clientInfo;

            UriBuilder uriBuilder = new(_providerInfo)
            {
                Path = "auth/realms/HyperID/.well-known/openid-configuration"
            };

            HttpResponseMessage response = await transport.RequestGetAsync(uriBuilder.ToString(), cancellationToken_);
            try
            {
                _discover = await response.Content.ReadFromJsonAsync<Discovery>(options: null, cancellationToken_);

                response.Dispose();
            }
            catch (Exception ex)
            {
                if (ex is TaskCanceledException)
                {
                    throw;
                }
                else
                {
                    throw new HyperIDSDKException(ex.Message, ex);
                }
            }
        }
        /// <summary>
        /// Done
        /// </summary>
        public void Done()
        {
            transport.Done();
        }

        #region Authorization start interface impl

        /// <summary>
        /// StartAuthorizeSignIn
        /// Flow 0
        /// </summary>
        string IHyperIDSDKAuth.StartSignInWeb2([AllowNull] KycVerificationLevel? kycVerificationLevel)
        {
            return AuthorizationUrlPrepare(flowMode: FlowMode.WEB2_SIGN_IN,
                kycVerificationLevel: kycVerificationLevel);
        }
        string IHyperIDSDKAuth.StartSignInWeb3([AllowNull] WalletFamily? walletFamily,
            [AllowNull] KycVerificationLevel? kycVerificationLevel)
        {
            int? walletFamilyValue = null;
            if (walletFamily.HasValue)
            {
                walletFamilyValue = (int?)walletFamily.Value;
            }

            return AuthorizationUrlPrepare(flowMode: FlowMode.WEB3_SIGN_IN,
                walletFamily: walletFamilyValue,
                kycVerificationLevel: kycVerificationLevel);
        }
        string IHyperIDSDKAuth.StartWalletGet([AllowNull] WalletGetMode walletGetMode,
            [AllowNull] WalletFamily? walletFamily)
        {
            int? walletFamilyId = null;
            if (walletFamily.HasValue)
            {
                walletFamilyId = (int?)walletFamily.Value;
            }

            return AuthorizationUrlPrepare(flowMode: FlowMode.WALLET_GET,
                walletGetMode: walletGetMode,
                walletFamily: walletFamilyId);
        }
        string IHyperIDSDKAuth.StartSignInGuestUpgrade()
        {
            return AuthorizationUrlPrepare(flowMode: FlowMode.UPGRADE_FROM_GUEST);
        }
        string IHyperIDSDKAuth.StartSignInIdentityProvider([NotNull] string identyProvider)
        {
            ArgumentNullException.ThrowIfNull(identyProvider);

            return AuthorizationUrlPrepare(flowMode: FlowMode.IDENTITY_PROVIDER,
                identityProvider: identyProvider);
        }
        #endregion

        /// <summary>
        /// IHyperIDSDKAuth.OnStartAuthorize
        /// </summary>
        async Task<bool> IHyperIDSDKAuth.CompleteSignInAsync([NotNull] string redirectUri,
            CancellationToken cancellationToken)
        {
            ArgumentNullException.ThrowIfNull(redirectUri);

            EnsureInitialised();

            NameValueCollection queryString;
            try
            {
                Uri uri = new Uri(redirectUri);
                queryString = HttpUtility.ParseQueryString(uri.Query);
            }
            catch
            {
                throw new HyperIDSDKException("Invalid redirect URL");
            }

            string? code = queryString["code"];
            if (string.IsNullOrWhiteSpace(code))
            {
                throw new HyperIDSDKException("Redirect URL contain invalid code");
            }

            
            List<KeyValuePair<string, string>> queryParams = AssertionPrepare();
            queryParams.Add(new KeyValuePair<string, string>("grant_type", JwtNames.QueryValueAuthorizationCode));
            queryParams.Add(new KeyValuePair<string, string>("redirect_uri", _clientInfo.RedirectUri));
            queryParams.Add(new KeyValuePair<string, string>("code", code));

            HttpContent content = new FormUrlEncodedContent(queryParams);
            UriBuilder uriBuilder = new UriBuilder(_discover.TokenEndpoint);
                
            HttpResponseMessage response = await transport.RequestPostAsync(uriBuilder.Uri,
                content,
                null,
                cancellationToken);

            try
            {
                JsonResponceAuthComplete? jsonResponse = await response.Content.ReadFromJsonAsync<JsonResponceAuthComplete>(options: null, cancellationToken);
                if (jsonResponse != null)
                {
                    _accessToken = jsonResponse.AccessToken;
                    _refreshToken = jsonResponse.RefreshToken;

                    return true;
                }
            }
            catch (Exception ex)
            {
                if (ex is TaskCanceledException)
                {
                    throw;
                }
                else
                {
                    throw new HyperIDSDKException(ex.Message, ex);
                }
            }
            return false;
        }
        UserInfo? IHyperIDSDKAuth.UserInfo()
        {
            EnsureAuthorized();

            try
            {
                var handler = new JwtSecurityTokenHandler();
                JwtSecurityToken? accessToken = handler.ReadToken(_accessToken) as JwtSecurityToken;
                if (accessToken != null)
                {
                    WalletInfo? walletInfo = null;

                    Claim? walletClaim = accessToken.Claims.FirstOrDefault(claim => claim.Type == "wallet_address");
                    if (walletClaim != null)
                    {
                        string? walletAddress = walletClaim.Value;
                        if (!string.IsNullOrWhiteSpace(walletAddress))
                        {
                            var claimWalletSource = accessToken.Claims.FirstOrDefault(claim => claim.Type == "wallet_source");
                            var claimIsWalletVerified = accessToken.Claims.FirstOrDefault(claim => claim.Type == "is_wallet_verified");
                            var claimWalletFamily = accessToken.Claims.FirstOrDefault(claim => claim.Type == "wallet_family");

                            int walletSource = -1;
                            if(claimWalletSource != null)
                            {
                                walletSource = int.Parse(claimWalletSource.Value);
                            }
                            bool isWalletVerified = false;
                            if(claimIsWalletVerified != null)
                            {
                                isWalletVerified = bool.Parse(claimIsWalletVerified.Value);
                            }
                            int walletFamily = -1;
                            if(claimWalletFamily != null)
                            {
                                walletFamily = int.Parse(claimWalletFamily.Value);
                            }

                            walletInfo = new WalletInfo(walletAddress,
                                accessToken.Claims.FirstOrDefault(claim => claim.Type == "wallet_chain_id")?.Value,
                                walletSource,
                                isWalletVerified,
                                walletFamily,
                                accessToken.Claims.FirstOrDefault(claim => claim.Type == "wallet_tags")?.Value);
                        }
                    }

                    foreach (var claim in accessToken.Claims)
                    {
                        var name = claim.ToString();
                        var value = claim.Value;
                    }

                    var claimSub = accessToken.Claims.FirstOrDefault(claim => claim.Type == "sub");
                    var claimIsGuest = accessToken.Claims.FirstOrDefault(claim => claim.Type == "is_guest");
                    var claimEmail = accessToken.Claims.FirstOrDefault(claim => claim.Type == "email");
                    var claimEmailVerfied = accessToken.Claims.FirstOrDefault(claim => claim.Type == "email_verified");
                    var claimDeviceId = accessToken.Claims.FirstOrDefault(claim => claim.Type == "device_id");
                    var claimIp = accessToken.Claims.FirstOrDefault(claim => claim.Type == "ip");

                    string? sub = claimSub?.Value;
                    bool isGuest = false;
                    if(claimIsGuest != null)
                    {
                        isGuest = bool.Parse(claimIsGuest.Value);
                    }
                    string? email = claimEmail?.Value;
                    bool emailVerfied = false;
                    if(claimEmailVerfied != null)
                    {
                        emailVerfied = bool.Parse(claimEmailVerfied.Value);
                    }
                    string? deviceId = claimDeviceId?.Value;
                    string? ip = claimIp?.Value;

                    return new UserInfo(sub,
                        isGuest,
                        email,
                        emailVerfied,
                        deviceId,
                        ip,
                        walletInfo);
                }
            }
            catch (Exception ex)
            {
                if (ex is TaskCanceledException)
                {
                    throw;
                }
                else
                {
                    throw new HyperIDSDKException(ex.Message, ex);
                }
            }
            return null;
        }

        async Task IHyperIDSDKAuth.SignOutAsync(CancellationToken cancellationToken)
        {
            EnsureAuthorized();

            string errorContent = "";
                UriBuilder uriBuilder = new UriBuilder(_discover!.SessionEndEndpoint);
                var queryParams = AssertionPrepare();
                queryParams.Add(new KeyValuePair<string, string>("refresh_token", _refreshToken));
                HttpContent requestContent = new FormUrlEncodedContent(queryParams);

                HttpResponseMessage response = await transport.RequestPostAsync(uriBuilder.Uri,
                    requestContent,
                    null,
                    cancellationToken);
        }
        public List<string> IdentityProviders()
        {
            EnsureInitialised();

            return _discover.IdentityProviders;
        }

        private string AuthorizationUrlPrepare(
           FlowMode flowMode,
           WalletGetMode? walletGetMode = null,
           int? walletFamily = null,
           string? walletAddress = null,
           KycVerificationLevel? kycVerificationLevel = null,
           string? identityProvider = null)
        {
            EnsureInitialised();

            UriBuilder uriBuilder = new UriBuilder(_discover!.AuthEndpoint);
            NameValueCollection query = HttpUtility.ParseQueryString("");

            query.Add("flow_mode", ((int)flowMode).ToString());
            query.Add("response_type", "code");
            query.Add("scope", string.Join(' ', _discover.Scopes()));
            query.Add("client_id", _clientInfo.ClientId);
            query.Add("redirect_uri", _clientInfo.RedirectUri);
            if (walletGetMode != null && walletGetMode.HasValue)
            {
                query.Add("wallet_get_mode", walletGetMode.GetValueOrDefault().ToString("D"));
            }
            if (walletFamily != null)
            {
                query.Add("wallet_family", walletFamily.GetValueOrDefault().ToString("D"));
            }
            if (!string.IsNullOrWhiteSpace(walletAddress))
            {
                query.Add("wallet_address", walletAddress);
            }
            if (kycVerificationLevel != null && kycVerificationLevel.HasValue)
            {
                query.Add("verification_level", kycVerificationLevel.GetValueOrDefault().ToString("D"));
            }
            if (!string.IsNullOrWhiteSpace(identityProvider))
            {
                query.Add("identity_provider", identityProvider);
            }
            uriBuilder.Query = query.ToString();

            return uriBuilder.ToString();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        /// <exception cref="HyperIDAuthException"></exception>
        private List<KeyValuePair<string, string>> AssertionPrepare()
        {
            //do not create NameValueCollection manually - it do not creqte query to link
            //HttpUtility.ParseQueryString create private class with ToString overriden
            List<KeyValuePair<string, string>> query = new List<KeyValuePair<string, string>>(); //HttpUtility.ParseQueryString("");

            if (_clientInfo.AuthMethod == AuthMethod.CLIENT_SECRET_BASIC)
            {
                query.Add(new KeyValuePair<string, string>("client_id", _clientInfo.ClientId));
                query.Add(new KeyValuePair<string, string>("client_secret", _clientInfo.ClientSecret!));
            }
            else
            {
                string jtiGuid = Guid.NewGuid().ToString();
                ClaimsIdentity claims = new(new[]
                {
                    new Claim(JwtRegisteredClaimNames.Iss, _clientInfo.ClientId),
                    new Claim(JwtRegisteredClaimNames.Sub, _clientInfo.ClientId),
                    new Claim(JwtRegisteredClaimNames.Jti, jtiGuid),
                    new Claim(JwtRegisteredClaimNames.Iat, eTime.Now()),
                    new Claim(JwtRegisteredClaimNames.Exp, eTime.NowPlusHour()),
                    new Claim(JwtRegisteredClaimNames.Aud, _discover!.Issuer)
                });

                try
                {
                    SecurityKey securityKey;
                    SigningCredentials signingCredentials;

                    if (_clientInfo.AuthMethod == AuthMethod.CLIENT_SECRET_HMAC)
                    {
                        securityKey = new SymmetricSecurityKey(System.Text.Encoding.UTF8.GetBytes(_clientInfo.ClientSecret!));
                        signingCredentials = new SigningCredentials(securityKey, SecurityAlgorithms.HmacSha256);
                    }
                    else
                    {
                        var rsa = RSA.Create();
                        rsa.ImportFromPem(_clientInfo.RSAKeysPem);

                        securityKey = new RsaSecurityKey(rsa.ExportParameters(includePrivateParameters: true)); ;
                        signingCredentials = new SigningCredentials(securityKey, SecurityAlgorithms.RsaSha256);
                    }

                    signingCredentials.Key.KeyId = "";

                    var token = new JwtSecurityToken(claims: claims.Claims,
                                                     signingCredentials: signingCredentials);


                    query.Add(new KeyValuePair<string, string>("client_assertion_type", JwtNames.QueryValueClientAssertionType));
                    query.Add(new KeyValuePair<string, string>("client_assertion", new JwtSecurityTokenHandler().WriteToken(token)));
                }
                catch (Exception ex)
                {
                    throw new HyperIDSDKException("Invalid assertion params", ex);
                }
            }
            return query;
        }
        private void EnsureInitialised()
        {
            if (string.IsNullOrEmpty(_providerInfo)
                || !_clientInfo.IsValid()
                || _discover == null)
            {
                throw new HyperIDSDKExceptionInitRequired();
            }
        }
        private void EnsureAuthorized()
        {
            EnsureInitialised();

            if (_refreshToken == null)
            {
                throw new HyperIDSDKExceptionAuthRequired();
            }
        }

        private async Task<string> AccessToken(CancellationToken cancellationToken)
        {
            EnsureAuthorized();

            if (_accessToken == null || IsTokenExpired(_accessToken))
            {
                return await RefreshToken(cancellationToken);
            }
            return _accessToken;
        }
        private async Task<string> RefreshToken(CancellationToken cancellationToken)
        {
            EnsureAuthorized();

            if (_refreshToken == null || IsTokenExpired(_refreshToken))
            {
                throw new HyperIDSDKException("SDK is not authorized");
            }

            List<KeyValuePair<string, string>> queryParams = AssertionPrepare();
            queryParams.Add(new KeyValuePair<string, string>("grant_type", "refresh_token"));
            queryParams.Add(new KeyValuePair<string, string>("refresh_token", _refreshToken));
            queryParams.Add(new KeyValuePair<string, string>("redirect_uri", _clientInfo.RedirectUri));

            HttpContent content = new FormUrlEncodedContent(queryParams);
            UriBuilder uriBuilder = new UriBuilder(_discover.TokenEndpoint);

            HttpResponseMessage response = await transport.RequestPostAsync(uriBuilder.Uri,
                content, 
                null,
                cancellationToken);
            try
            {
                JsonResponceAuthComplete? jsonResponse = await response.Content.ReadFromJsonAsync<JsonResponceAuthComplete>(options: null, cancellationToken);
                if (jsonResponse != null)
                {
                    _accessToken = jsonResponse.AccessToken;
                    _refreshToken = jsonResponse.RefreshToken;

                    return _accessToken;
                }
                else
                {
                    throw new HyperIDSDKException("Service answer is not valid ");
                }
            }
            catch (Exception ex)
            {
                if (ex is TaskCanceledException)
                {
                    throw;
                }
                else
                {
                    throw new HyperIDSDKException(ex.Message, ex);
                }
            }
        }
        
        private bool IsTokenExpired(string token)
        {
            JwtSecurityToken jwtSecurityToken;
            try
            {
                jwtSecurityToken = new JwtSecurityToken(token);
            }
            catch (Exception)
            {
                return false;
            }

            return jwtSecurityToken.ValidTo <= DateTime.UtcNow;
        }
        async Task<HttpResponseMessage> IHyperIDSDKAuthRestApi.RestApiGetRequestAsync(CancellationToken cancellationToken)
        {
            var accessToken = await AccessToken(cancellationToken);

            EnsureAuthorized();

            if (accessToken != null)
            {
                UriBuilder uriBuilder = new UriBuilder(_discover.RestApiTokenEndpoint);

                return await transport.RequestGetAsync(uriBuilder.ToString(),
                    cancellationToken);
            }
            else
            {
                throw new HyperIDSDKExceptionAuthRequired();
            }
        }
        async Task<HttpResponseMessage> IHyperIDSDKAuthRestApi.RestApiPostRequestAsync(RestApiRequest request,
            CancellationToken cancellationToken)
        {
            var accessToken = await AccessToken(cancellationToken);

            EnsureAuthorized();

            if (accessToken != null)
            {
                Uri baseUri = new(_discover!.RestApiTokenEndpoint);
                Uri requestUri = new(baseUri, request.UriPath);

                return await transport.RequestPostAsync(requestUri,
                    request.Content,
                    accessToken,
                    cancellationToken);
            }
            else
            {
                throw new HyperIDSDKExceptionAuthRequired();
            }
        }

        public string? AccessToken() => _accessToken;
        public string? RefreshToken() => _refreshToken;
    }
}//namespace HyperId.Private