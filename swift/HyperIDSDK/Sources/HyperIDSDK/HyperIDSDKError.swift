import Foundation

//**************************************************************************************************
//	MARK: HyperIDSDKError
//--------------------------------------------------------------------------------------------------
public enum HyperIDSDKError : Error {
	case authorizationExpired
}

//**************************************************************************************************
//	MARK: HyperIDSDKError
//--------------------------------------------------------------------------------------------------
extension HyperIDSDKError : LocalizedError {
	public var errorDescription: String? {
		switch self {
		case .authorizationExpired:				"HyperID authorization expired. Please authorize yourself."
		}
	}
}
