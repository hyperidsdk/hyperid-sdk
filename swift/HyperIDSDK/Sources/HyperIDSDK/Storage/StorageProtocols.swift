import Foundation
import HyperIDBase

//**************************************************************************************************
//	StorageResultProtocol
//--------------------------------------------------------------------------------------------------
protocol StorageResultProtocol : Validatable {
	init(rawValue: Int64)
}
//**************************************************************************************************
//	StorageSharedDataKeysListResponseProtocol
//--------------------------------------------------------------------------------------------------
protocol StorageSharedDataKeysListResponseProtocol : HyperIDResponseBase {
	var keys				: [String]	{ get }
	var nextSearchId		: String	{ get }
}
//**************************************************************************************************
//	StorageResultProtocol
//--------------------------------------------------------------------------------------------------
protocol StorageDataKeysListResponseProtocol : HyperIDResponseBase {
	var keysPrivate			: [String] { get }
	var keysPublic			: [String] { get }
}
//**************************************************************************************************
//	StorageUserDataGetResponseProtocol
//--------------------------------------------------------------------------------------------------
protocol StorageUserDataResponseProtocol : HyperIDResponseBase {
	var values				: [String] { get }
}
