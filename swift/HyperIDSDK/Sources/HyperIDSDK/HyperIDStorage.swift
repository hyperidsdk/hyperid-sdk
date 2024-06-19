import Foundation
import HyperIDBase

//**************************************************************************************************
//	HyperIDStorage
//--------------------------------------------------------------------------------------------------
public enum HyperIDStorage {
	case email
	case userID
	case wallet(address: String)
	case identityProvider(_ identityProvider : String)
}
