import Foundation

//**************************************************************************************************
//	MARK: HyperIDRequestUtils
//--------------------------------------------------------------------------------------------------
public class HyperIDRequestUtils {
	//==================================================================================================
	//	constructBaseRequest
	//--------------------------------------------------------------------------------------------------
	public static func constructBaseRequest(_ url		: URL,
											accessToken: String) -> URLRequest {
		var urlRequest			= URLRequest(url: url)
		urlRequest.httpMethod	= "POST"
		urlRequest.allHTTPHeaderFields = [
			"Accept":			"application/json",
			"Content-Type":		"application/json",
			"Authorization":	"Bearer \(accessToken)",
		]
		return urlRequest
	}
}
