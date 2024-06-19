using System;
using System.Diagnostics.CodeAnalysis;

namespace HyperId.SDK
{
    public class HyperIDSDKException : Exception
    {
        public HyperIDSDKException(
            [AllowNull] string? message_ = null,
            [AllowNull] Exception? innerException_ = null) : base(message_, innerException_)
        {}
    }

    internal class HyperIDSDKExceptionInitRequired : HyperIDSDKException
    {
        public HyperIDSDKExceptionInitRequired([AllowNull] Exception? innerException_ = null) : base("Authorizatioin SDK init required", innerException_)
        {}
    }

    internal class HyperIDSDKExceptionAuthRequired : HyperIDSDKException
    {
        public HyperIDSDKExceptionAuthRequired([AllowNull] Exception? innerException_ = null) : base("Authorization required", innerException_)
        {}
    }

    internal class HyperIDSDKExceptionUnderMaintenace : HyperIDSDKException
    {
        public HyperIDSDKExceptionUnderMaintenace([AllowNull] Exception? innerException_ = null) : base("Service is under maintenence", innerException_)
        {}
    }

    internal class HyperIDSDKExceptionHyperIDAuthenticatorNotFound : HyperIDSDKException
    {
        public HyperIDSDKExceptionHyperIDAuthenticatorNotFound([AllowNull] Exception? innerException_ = null) : base("Failure due to the user's device with HyperID Authenticator App not being found", innerException_)
        {}
    }

}//namespace HyperId.SDK