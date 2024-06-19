
namespace hyperid_sdk_demo.Model
{
    public enum AuthSDKState
    {
        CREATED     = 0,
        INITIALISED = 1,
        AUTHORIZED  = 2,
    }

    public enum MfaSDKState
    {
        CREATED                 = 0,
        AVAILABILITY_CHECKED    = 1,
        TRANSACTION_CREATED    = 2
    }
}