import invariant from 'invariant';
import { NativeModules, Platform } from 'react-native';
import base64 from 'react-native-base64';

const { RNAppAuth } = NativeModules;

const validateIssuerOrServiceConfigurationEndpoints = (issuer, serviceConfiguration) =>
  invariant(
    typeof issuer === 'string' ||
      (serviceConfiguration &&
        typeof serviceConfiguration.authorizationEndpoint === 'string' &&
        typeof serviceConfiguration.tokenEndpoint === 'string'),
    'Config error: you must provide either an issuer or a service endpoints'
  );
const validateIssuerOrServiceConfigurationRegistrationEndpoint = (issuer, serviceConfiguration) =>
  invariant(
    typeof issuer === 'string' ||
      (serviceConfiguration && typeof serviceConfiguration.registrationEndpoint === 'string'),
    'Config error: you must provide either an issuer or a registration endpoint'
  );
const validateIssuerOrServiceConfigurationRevocationEndpoint = (issuer, serviceConfiguration) =>
  invariant(
    typeof issuer === 'string' ||
      (serviceConfiguration && typeof serviceConfiguration.revocationEndpoint === 'string'),
    'Config error: you must provide either an issuer or a revocation endpoint'
  );
const validateIssuerOrServiceConfigurationEndSessionEndpoint = (issuer, serviceConfiguration) =>
  invariant(
    typeof issuer === 'string' ||
      (serviceConfiguration && typeof serviceConfiguration.endSessionEndpoint === 'string'),
    'Config error: you must provide either an issuer or an end session endpoint'
  );
const validateClientId = clientId =>
  invariant(typeof clientId === 'string', 'Config error: clientId must be a string');
const validateRedirectUrl = redirectUrl =>
  invariant(typeof redirectUrl === 'string', 'Config error: redirectUrl must be a string');

const validateHeaders = headers => {
  if (!headers) {
    return;
  }
  const customHeaderTypeErrorMessage =
    'Config error: customHeaders type must be { token?: { [key: string]: string }, authorize?: { [key: string]: string }, register: { [key: string]: string }}';

  const authorizedKeys = ['token', 'authorize', 'register'];
  const keys = Object.keys(headers);
  const correctKeys = keys.filter(key => authorizedKeys.includes(key));
  invariant(
    keys.length <= authorizedKeys.length &&
      correctKeys.length > 0 &&
      correctKeys.length === keys.length,
    customHeaderTypeErrorMessage
  );

  Object.values(headers).forEach(value => {
    invariant(typeof value === 'object', customHeaderTypeErrorMessage);
    invariant(
      Object.values(value).filter(key => typeof key !== 'string').length === 0,
      customHeaderTypeErrorMessage
    );
  });
};

const validateAdditionalHeaders = headers => {
  if (!headers) {
    return;
  }

  const errorMessage = 'Config error: additionalHeaders must be { [key: string]: string }';

  invariant(typeof headers === 'object', errorMessage);
  invariant(
    Object.values(headers).filter(key => typeof key !== 'string').length === 0,
    errorMessage
  );
};

const validateConnectionTimeoutSeconds = timeout => {
  if (!timeout) {
    return;
  }

  invariant(typeof timeout === 'number', 'Config error: connectionTimeoutSeconds must be a number');
};

export const SECOND_IN_MS = 1000;
export const DEFAULT_TIMEOUT_IOS = 60;
export const DEFAULT_TIMEOUT_ANDROID = 15;

const convertTimeoutForPlatform = (
  platform,
  connectionTimeout = Platform.OS === 'ios' ? DEFAULT_TIMEOUT_IOS : DEFAULT_TIMEOUT_ANDROID
) => (platform === 'android' ? connectionTimeout * SECOND_IN_MS : connectionTimeout);

export const prefetchConfiguration = async ({
  warmAndPrefetchChrome = false,
  issuer,
  redirectUrl,
  clientId,
  scopes,
  serviceConfiguration,
  dangerouslyAllowInsecureHttpRequests = false,
  customHeaders,
  connectionTimeoutSeconds,
}) => {
  if (Platform.OS === 'android') {
    validateIssuerOrServiceConfigurationEndpoints(issuer, serviceConfiguration);
    validateClientId(clientId);
    validateRedirectUrl(redirectUrl);
    validateHeaders(customHeaders);
    validateConnectionTimeoutSeconds(connectionTimeoutSeconds);

    const nativeMethodArguments = [
      warmAndPrefetchChrome,
      issuer,
      redirectUrl,
      clientId,
      scopes,
      serviceConfiguration,
      dangerouslyAllowInsecureHttpRequests,
      customHeaders,
      convertTimeoutForPlatform(Platform.OS, connectionTimeoutSeconds),
    ];

    RNAppAuth.prefetchConfiguration(...nativeMethodArguments);
  }
};

export const register = ({
  issuer,
  redirectUrls,
  responseTypes,
  grantTypes,
  subjectType,
  tokenEndpointAuthMethod,
  additionalParameters,
  serviceConfiguration,
  dangerouslyAllowInsecureHttpRequests = false,
  customHeaders,
  additionalHeaders,
  connectionTimeoutSeconds,
}) => {
  validateIssuerOrServiceConfigurationRegistrationEndpoint(issuer, serviceConfiguration);
  validateHeaders(customHeaders);
  validateAdditionalHeaders(additionalHeaders);
  validateConnectionTimeoutSeconds(connectionTimeoutSeconds);

  invariant(
    Array.isArray(redirectUrls) && redirectUrls.every(url => typeof url === 'string'),
    'Config error: redirectUrls must be an Array of strings'
  );
  invariant(
    responseTypes == null ||
      (Array.isArray(responseTypes) && responseTypes.every(rt => typeof rt === 'string')),
    'Config error: if provided, responseTypes must be an Array of strings'
  );
  invariant(
    grantTypes == null ||
      (Array.isArray(grantTypes) && grantTypes.every(gt => typeof gt === 'string')),
    'Config error: if provided, grantTypes must be an Array of strings'
  );
  invariant(
    subjectType == null || typeof subjectType === 'string',
    'Config error: if provided, subjectType must be a string'
  );
  invariant(
    tokenEndpointAuthMethod == null || typeof tokenEndpointAuthMethod === 'string',
    'Config error: if provided, tokenEndpointAuthMethod must be a string'
  );

  const nativeMethodArguments = [
    issuer,
    redirectUrls,
    responseTypes,
    grantTypes,
    subjectType,
    tokenEndpointAuthMethod,
    additionalParameters,
    serviceConfiguration,
    convertTimeoutForPlatform(Platform.OS, connectionTimeoutSeconds),
  ];

  if (Platform.OS === 'android') {
    nativeMethodArguments.push(dangerouslyAllowInsecureHttpRequests);
    nativeMethodArguments.push(customHeaders);
  }

  if (Platform.OS === 'ios') {
    nativeMethodArguments.push(additionalHeaders);
  }

  return RNAppAuth.register(...nativeMethodArguments);
};

export const authorize = ({
  issuer,
  redirectUrl,
  clientId,
  clientSecret,
  scopes,
  useNonce = true,
  usePKCE = true,
  additionalParameters,
  serviceConfiguration,
  clientAuthMethod = 'basic',
  dangerouslyAllowInsecureHttpRequests = false,
  customHeaders,
  additionalHeaders,
  skipCodeExchange = false,
  iosCustomBrowser = null,
  androidAllowCustomBrowsers = null,
  androidTrustedWebActivity = false,
  connectionTimeoutSeconds,
  iosPrefersEphemeralSession = false,
}) => {
  validateIssuerOrServiceConfigurationEndpoints(issuer, serviceConfiguration);
  validateClientId(clientId);
  validateRedirectUrl(redirectUrl);
  validateHeaders(customHeaders);
  validateAdditionalHeaders(additionalHeaders);
  validateConnectionTimeoutSeconds(connectionTimeoutSeconds);
  // TODO: validateAdditionalParameters

  const nativeMethodArguments = [
    issuer,
    redirectUrl,
    clientId,
    clientSecret,
    scopes,
    additionalParameters,
    serviceConfiguration,
    skipCodeExchange,
    convertTimeoutForPlatform(Platform.OS, connectionTimeoutSeconds),
  ];

  if (Platform.OS === 'android') {
    nativeMethodArguments.push(useNonce);
    nativeMethodArguments.push(usePKCE);
    nativeMethodArguments.push(clientAuthMethod);
    nativeMethodArguments.push(dangerouslyAllowInsecureHttpRequests);
    nativeMethodArguments.push(customHeaders);
    nativeMethodArguments.push(androidAllowCustomBrowsers);
    nativeMethodArguments.push(androidTrustedWebActivity);
  }

  if (Platform.OS === 'ios') {
    nativeMethodArguments.push(additionalHeaders);
    nativeMethodArguments.push(useNonce);
    nativeMethodArguments.push(usePKCE);
    nativeMethodArguments.push(iosCustomBrowser);
    nativeMethodArguments.push(iosPrefersEphemeralSession);
  }

  return RNAppAuth.authorize(...nativeMethodArguments);
};

export const refresh = (
  {
    issuer,
    redirectUrl,
    clientId,
    clientSecret,
    scopes,
    additionalParameters = {},
    serviceConfiguration,
    clientAuthMethod = 'basic',
    dangerouslyAllowInsecureHttpRequests = false,
    customHeaders,
    additionalHeaders,
    iosCustomBrowser = null,
    androidAllowCustomBrowsers = null,
    connectionTimeoutSeconds,
  },
  { refreshToken }
) => {
  validateIssuerOrServiceConfigurationEndpoints(issuer, serviceConfiguration);
  validateClientId(clientId);
  validateRedirectUrl(redirectUrl);
  validateHeaders(customHeaders);
  validateAdditionalHeaders(additionalHeaders);
  validateConnectionTimeoutSeconds(connectionTimeoutSeconds);
  invariant(refreshToken, 'Please pass in a refresh token');
  // TODO: validateAdditionalParameters

  const nativeMethodArguments = [
    issuer,
    redirectUrl,
    clientId,
    clientSecret,
    refreshToken,
    scopes,
    additionalParameters,
    serviceConfiguration,
    convertTimeoutForPlatform(Platform.OS, connectionTimeoutSeconds),
  ];

  if (Platform.OS === 'android') {
    nativeMethodArguments.push(clientAuthMethod);
    nativeMethodArguments.push(dangerouslyAllowInsecureHttpRequests);
    nativeMethodArguments.push(customHeaders);
    nativeMethodArguments.push(androidAllowCustomBrowsers);
  }

  if (Platform.OS === 'ios') {
    nativeMethodArguments.push(additionalHeaders);
    nativeMethodArguments.push(iosCustomBrowser);
  }

  return RNAppAuth.refresh(...nativeMethodArguments);
};

export const revoke = async (
  { clientId, issuer, serviceConfiguration, clientSecret },
  { tokenToRevoke, sendClientId = false, includeBasicAuth = false }
) => {
  invariant(tokenToRevoke, 'Please include the token to revoke');
  validateClientId(clientId);
  validateIssuerOrServiceConfigurationRevocationEndpoint(issuer, serviceConfiguration);

  let revocationEndpoint;
  if (serviceConfiguration && serviceConfiguration.revocationEndpoint) {
    revocationEndpoint = serviceConfiguration.revocationEndpoint;
  } else {
    const response = await fetch(`${issuer}/.well-known/openid-configuration`);
    const openidConfig = await response.json();

    invariant(
      openidConfig.revocation_endpoint,
      'The openid config does not specify a revocation endpoint'
    );

    revocationEndpoint = openidConfig.revocation_endpoint;
  }

  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
  };
  if (includeBasicAuth) {
    headers.Authorization = `Basic ${base64.encode(`${clientId}:${clientSecret}`)}`;
  }
  /**
    Identity Server insists on client_id being passed in the body,
    but Google does not. According to the spec, Google is right
    so defaulting to no client_id
    https://tools.ietf.org/html/rfc7009#section-2.1
  **/
  return await fetch(revocationEndpoint, {
    method: 'POST',
    headers,
    body: `token=${tokenToRevoke}${sendClientId ? `&client_id=${clientId}` : ''}`,
  }).catch(error => {
    throw new Error('Failed to revoke token', error);
  });
};


export const dataSet = async (
  { restApiEndpoint },
  { accessToken, dataKey, dataValue }
) => {
  invariant(restApiEndpoint, 'Please include rest api endpoint');
  invariant(accessToken, 'Please include the token to rest api authorization');

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`,
  };

  const content = {
    "value_key": dataKey,
    "value_data": dataValue
  };

  const endpoint = `${restApiEndpoint}/user-data/set`

  let response = await fetch(endpoint, {
    method: 'POST',
    headers : headers,
    body: JSON.stringify(content),
  }).catch(error => {
    throw new Error('Data set failed', error);
  });

  try {
    let responseJson = await response.json()

    return { result : responseJson.result }
  }
  catch(error){
    return { result : 1}
  }
};


export const dataGet = async (
  { restApiEndpoint },
  { accessToken, dataKey }
) => {
  invariant(restApiEndpoint, 'Please include rest api endpoint');
  invariant(accessToken, 'Please include the token to rest api authorization');
  invariant(dataKey, 'Please include the dataKey field');

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`,
  };

  const content = {
    "value_keys": [ dataKey ],
  };
  
  const endpoint = `${restApiEndpoint}/user-data/get`

  let response = await fetch(endpoint, {
    method: 'POST',
    headers: headers,
    body: JSON.stringify(content),
  }).catch(error => {
    throw new Error('Data Get failed', error);
  });

  try {
    let responseJson = await response.json()
    return {
      result: responseJson.result,
      dataValues: responseJson.values
     }
  }
  catch(error){
    return { result : 1}
  }
};

export const walletsGet = async (
  { restApiEndpoint },
  { accessToken }
) => {
  invariant(restApiEndpoint, 'Please include rest api endpoint');
  invariant(accessToken, 'Please include the token to rest api authorization');

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`,
  };

  let response = await fetch(`${restApiEndpoint}/user-wallets/get`, {
    method: 'POST',
    headers,
    body: JSON.stringify({}),
  }).catch(error => {
    throw new Error('Failed to wallets get', error);
  });
  
  try {
    let responseJson = await response.json();

    let walletsInfo = [];
    responseJson.wallets_private.forEach(walletInfoJson => {
      let info = {
        isPublic: false,
        address: walletInfoJson.address,
        chain: walletInfoJson.chain,
        family: walletInfoJson.family,
        label: walletInfoJson.label,
        tags: walletInfoJson.tags,
      };
      walletsInfo.push(info);
    })

    responseJson.wallets_public.forEach(walletInfoJson => {
      let info = {
        isPublic: true,
        address: walletInfoJson.address,
        chain: walletInfoJson.chain,
        family: walletInfoJson.family,
        label: walletInfoJson.label,
        tags: walletInfoJson.tags
      }
      walletsInfo.push(info);
    })

    return {
      result: responseJson.result,
      walletsInfo: walletsInfo,
    }
  } catch(error)
  {
    throw new Error(`Failed to wallets get`, error);
  };
};

export const logout = (
  {
    issuer,
    serviceConfiguration,
    additionalParameters,
    dangerouslyAllowInsecureHttpRequests = false,
    iosCustomBrowser = null,
    iosPrefersEphemeralSession = false,
    androidAllowCustomBrowsers = null,
  },
  { idToken, postLogoutRedirectUrl }
) => {
  validateIssuerOrServiceConfigurationEndSessionEndpoint(issuer, serviceConfiguration);
  validateRedirectUrl(postLogoutRedirectUrl);
  invariant(idToken, 'Please pass in the ID token');

  const nativeMethodArguments = [
    issuer,
    idToken,
    postLogoutRedirectUrl,
    serviceConfiguration,
    additionalParameters,
  ];

  if (Platform.OS === 'android') {
    nativeMethodArguments.push(dangerouslyAllowInsecureHttpRequests);
    nativeMethodArguments.push(androidAllowCustomBrowsers);
  }

  if (Platform.OS === 'ios') {
    nativeMethodArguments.push(iosCustomBrowser);
    nativeMethodArguments.push(iosPrefersEphemeralSession);
  }

  return RNAppAuth.logout(...nativeMethodArguments);
};
