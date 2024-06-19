import Foundation

//**************************************************************************************************
//	MARK: ProviderInfo
//--------------------------------------------------------------------------------------------------
public class ProviderInfo {
	var scheme	: String
	var host	: String
	var port	: UInt16
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public required init(scheme: String, host: String, port: UInt16) {
		self.scheme = scheme
		self.host = host
		self.port = port
	}
	//==================================================================================================
	//	isValid
	//--------------------------------------------------------------------------------------------------
	public var isValid : Bool {
		!(scheme.isEmpty
		|| host.isEmpty)
	}
	public static var sandbox		: Self { Self.init(scheme: "https", host: "login-sandbox.hypersecureid.com",	port: 443) }
	public static var production	: Self { Self.init(scheme: "https", host: "login.hypersecureid.com",			port: 443) }
}
