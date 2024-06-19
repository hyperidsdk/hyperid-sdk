import XCTest
import HyperIDBase

//**************************************************************************************************
//	HyperIDBaseAPITests
//--------------------------------------------------------------------------------------------------
class HyperIDBaseAPITests : XCTestCase {
	//==================================================================================================
	//	testHyperIDAPIBaseByProvider
	//--------------------------------------------------------------------------------------------------
	func testHyperIDAPIBaseByProvider() async throws {
		let hyperIdApiBase = try await HyperIDBaseAPI(providerInfo: .production)
		XCTAssert(hyperIdApiBase.openIDConfiguration.isValid)
	}
	//==================================================================================================
	//	testHyperIDAPIBaseByProvider
	//--------------------------------------------------------------------------------------------------
	func testHyperIDAPIBaseByOpenIDConfiguration() async throws {
		let openIdConfiguration = try await OpenIDConfiguration.LoadOpenIDConfiguration(providerInfo: .production)
		let hyperIdApiBase = try await HyperIDBaseAPI(openIDConfiguration: openIdConfiguration)
		XCTAssert(hyperIdApiBase.openIDConfiguration.isValid)
	}
}
