using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public enum UserDataAccessScope
    {
        PRIVATE,
        PUBLIC
    }


    #region Email && UserId
    public enum DataSetResult
    {
        SUCCESS,
        FAIL_BY_KEY_INVALID,
        FAIL_BY_KEY_ACCESS_DENIED,
    }

    public enum DataGetRequestResult
    {
        SUCCESS,
        FAIL_BY_KEY_ACCESS_DENIED,
        FAIL_BY_KEYS_NOT_FOUND,
        FAIL_BY_TOO_MANY_KEYS_IN_REQUEST
    }
    #endregion


    #region Wallet
    public enum DataSetByWalletResult
    {
        SUCCESS,
        FAIL_BY_KEY_INVALID,
        FAIL_BY_KEY_ACCESS_DENIED,
        FAIL_WALLET_NOT_EXIST
    }

    public enum DataGetByWalletRequestResult
    {
        SUCCESS,
        FAIL_BY_KEY_ACCESS_DENIED,
        FAIL_BY_KEYS_NOT_FOUND,
        FAIL_BY_TOO_MANY_KEYS_IN_REQUEST,
        FAIL_BY_WALLET_NOT_FOUND
    }

    public enum KeysGetByWalletRequestResult
    {
        SUCCESS,
        FAIL_BY_WALLET_NOT_FOUND
    }

    public enum DataDeleteByWalletRequestResult
    {
        SUCCESS,
        FAIL_BY_WALLET_NOT_FOUND
    }
    #endregion


    #region Identity Provider
    public enum DataSetByIdentityProviderResult
    {
        SUCCESS,
        FAIL_BY_KEY_INVALID,
        FAIL_BY_KEY_ACCESS_DENIED,
        FAIL_IDENTITY_PROVIDER_NOT_EXIST
    }

    public enum DataGetByIdentityProviderRequestResult
    {
        SUCCESS,
        FAIL_BY_KEY_ACCESS_DENIED,
        FAIL_BY_KEYS_NOT_FOUND,
        FAIL_BY_TOO_MANY_KEYS_IN_REQUEST,
        FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND
    }

    public enum KeysGetByIdentityProviderRequestResult
    {
        SUCCESS,
        FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND
    }

    public enum DataDeleteByIdentityProviderRequestResult
    {
        SUCCESS,
        FAIL_BY_IDENTITY_PROVIDER_NOT_FOUND
    }
    #endregion
}
