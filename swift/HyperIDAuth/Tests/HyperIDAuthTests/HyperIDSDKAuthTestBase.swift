import XCTest
import HyperIDAuth
import HyperIDBase

//**************************************************************************************************
//	HyperIDSDKAuthTestsInit
//--------------------------------------------------------------------------------------------------
class HyperIDSDKAuthTestBase: XCTestCase {
	var hyperIdAuth : HyperIDAuthAPI!
	//==================================================================================================
	//	setUpWithError
	//--------------------------------------------------------------------------------------------------
	override func setUpWithError() throws {
		print("[HyperIDSDKAuthTestBase] Starting async library init")
		var errorCatched : Error?
		let errorSaver = { errorCatched = $0 }
		let expectation = expectation(description: "setup hyperIdAuth")
		Task {
			do {
				hyperIdAuth = try await FactoryHyperIDAPIAuth()
			} catch {
				errorSaver(error)
			}
			expectation.fulfill()
			print("[HyperIDSDKAuthTestBase] async library init finished")
		}
		wait(for: [expectation])
		if let errorCatched = errorCatched
		{
			print("[HyperIDSDKAuthTestBase] init error occured: \(errorCatched.localizedDescription)")
		}
		else
		{
			print("[HyperIDSDKAuthTestBase] Starting test")
		}
	}
	//==================================================================================================
	//	FactoryHyperIDAPIAuth
	//--------------------------------------------------------------------------------------------------
	func FactoryHyperIDAPIAuth() async throws -> HyperIDAuthAPI { fatalError() }
}
