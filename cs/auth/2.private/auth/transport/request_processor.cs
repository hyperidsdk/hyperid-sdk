using HyperId.SDK;
using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.Private
{
    internal class RequestProcessor
    {
        private HttpClient _httpClient = new HttpClient();

        public void Done()
        {
            _httpClient.Dispose();
        }
        public async Task<HttpResponseMessage> RequestGetAsync(string url, CancellationToken cancellationToken)
        {
            try
            {
                HttpResponseMessage response = await _httpClient.GetAsync(url, cancellationToken);
                try
                {
                    response.EnsureSuccessStatusCode();
                }
                catch (HttpRequestException ex)
                {
                    string body = "";
                    try
                    {
                        body = await response.Content.ReadAsStringAsync(cancellationToken);
                    }
                    catch (Exception)
                    { }

                    throw new HyperIDSDKException(ex.Message + " HyperIdResponceError = " + body, ex);
                }
                return response;
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
        public async Task<HttpResponseMessage> RequestPostAsync(Uri url,
            HttpContent? content,
            string? accessToken,
            CancellationToken cancellationToken = default)
        {
            try
            {
                var requestMessage = new HttpRequestMessage
                {
                    Method = HttpMethod.Post,
                    Content = content,
                    RequestUri = url,
                };

                var str = url.ToString();

                if (accessToken != null)
                {
                    requestMessage.Headers.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);
                }

                HttpResponseMessage response = await _httpClient.SendAsync(requestMessage, cancellationToken);

                try
                {
                    response.EnsureSuccessStatusCode();
                }
                catch (HttpRequestException ex)
                {
                    string body = "";
                    try
                    {
                        body = await response.Content.ReadAsStringAsync(cancellationToken);
                    }
                    catch (Exception)
                    { }

                    throw new HyperIDSDKExceptionUnderMaintenace(ex);
                }
                return response;
            }
            catch (Exception ex)
            {
                if (ex is TaskCanceledException || ex is HyperIDSDKExceptionUnderMaintenace)
                {
                    throw;
                }
                else
                {
                    throw new HyperIDSDKException(ex.Message, ex);
                }
            }
        }
    }
}//namespace HyperId.Private
