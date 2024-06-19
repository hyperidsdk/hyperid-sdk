import XCTest
import HyperIDSDK

//**************************************************************************************************
//	HyperIDSDKKYCTest
//--------------------------------------------------------------------------------------------------
final class HyperIDAPIKYCTest : XCTestCase {
	var hyperIdKYC		: HyperIDKYCAPI!
	var accessToken 	: String = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhMzQ3MzcyZS1mNjkwLTRiNmMtODQ4Yi0yY2I3NjM0NDdhNTMifQ.eyJleHAiOjE2OTkzNjQ2MzMsImlhdCI6MTY5OTM2MTAzMywiYXV0aF90aW1lIjoxNjk5MzYxMDE1LCJqdGkiOiJkNjIyZDIwNC00OTU1LTQzMjUtYmI0My1mMTQ3NGJmZDRiY2EiLCJpc3MiOiJodHRwczovL2xvZ2luLXN0YWdlLmh5cGVyc2VjdXJlaWQuY29tL2F1dGgvcmVhbG1zL0h5cGVySUQiLCJzdWIiOiJhYmU0MzU4Ny05MWVkLTRkNzMtODEwZi03ZGIyNWRiMzU3MTEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhbmRyb2lkLXNkay10ZXN0Iiwic2Vzc2lvbl9zdGF0ZSI6IjcxMjliNThjLTI2MzYtNDA1MS05MGFlLTE4Y2JjMGM2YTU3MyIsInJlZ2lvbl9pZCI6MCwic2NvcGUiOiJvcGVuaWQgdXNlci1kYXRhLXNldCBreWMtdmVyaWZpY2F0aW9uIGtleXMgdXNlci1pbmZvLWdldCBlbWFpbCBzZWNvbmQtZmFjdG9yLWF1dGgtY2xpZW50IGF1dGggbWZhLWNsaWVudCB1c2VyLWRhdGEtZ2V0IHdlYi1jaGF0Iiwic2lkIjoiNzEyOWI1OGMtMjYzNi00MDUxLTkwYWUtMThjYmMwYzZhNTczIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6ImU4MDRmM2U5YjEzNGE5MWZmNjZjNWE2MDkyNDNhNzVkOWIyNjM2OWU2MThhMmJhZTgzNGQ4MDM3MDBjZTBhZmYiLCJpcCI6IjMxLjEyOC4xNjIuODMiLCJkZXZpY2VfZGVzYyI6eyJjcHUiOnsiYXJjaGl0ZWN0dXJlIjoidW5rbm93biIsImNvcmVzIjo4fSwib3MiOnsidmVyc2lvbiI6Ik9TIFggMTBfMTVfNyIsInRpbWV6b25lIjotMTIwLCJ0eXBlIjoiRGVza3RvcCIsImRldmljZSI6eyJ2ZW5kb3IiOiJ1bmtub3duIiwibW9kZWwiOiJ1bmtub3duIn19LCJzY3JlZW4iOnsiZGVwdGgiOjI0LCJyYXRpbyI6MSwid2lkdGgiOjIwNTYsImhlaWdodCI6MTMyOSwic2l6ZSI6MjczMjQyNH0sImJyb3dzZXIiOnsibmFtZSI6IlNhZmFyaSIsInZlcnNpb24iOiI2MDUuMS4xNSJ9LCJhZ2VudCI6Ik1vemlsbGEvNS4wIChNYWNpbnRvc2g7IEludGVsIE1hYyBPUyBYIDEwXzE1XzcpIEFwcGxlV2ViS2l0LzYwNS4xLjE1IChLSFRNTCwgbGlrZSBHZWNrbykgVmVyc2lvbi8xNy4xIFNhZmFyaS82MDUuMS4xNSJ9LCJlbWFpbCI6ImFuZHJpaS5idXRva0BsaXN0YXQuY29tLnVhIn0.C1OIfcHVpaMK23GzVOw7yckuszQY24Fidw-DDfxUa5M"
	//==================================================================================================
	//	setUpWithError
	//--------------------------------------------------------------------------------------------------
	override func setUpWithError() throws {
		print("[HyperIDSDKKYCUserStatusGetTest] Starting async library init")
		var errorCatched : Error?
		let errorSaver = { errorCatched = $0 }
		let expectation = expectation(description: "setup hyperIdAuth")
		Task {
			do {
				hyperIdKYC = try await HyperIDKYCAPI(providerInfo: ProviderInfo.stage)
			} catch {
				errorSaver(error)
			}
			expectation.fulfill()
			print("[HyperIDSDKKYCUserStatusGetTest] async library init finished")
		}
		wait(for: [expectation])
		if let errorCatched = errorCatched
		{
			print("[HyperIDSDKKYCUserStatusGetTest] init error occured: \(errorCatched.localizedDescription)")
		}
		else
		{
			print("[HyperIDSDKKYCUserStatusGetTest] Starting test")
		}
	}
	//==================================================================================================
	//	testUserKYCStatusInfoGet
	//--------------------------------------------------------------------------------------------------
	func testGetUserKYCStatusInfo() async throws {
		let result = try await hyperIdKYC.getUserKYCStatusInfo(kycVerificationLevel: .full, accessToken: accessToken)
		print(result)
	}
	//==================================================================================================
	//	testUserKYCStatusTopLevelInfoGet
	//--------------------------------------------------------------------------------------------------
	func testGetUserKYCStatusTopLevelInfo() async throws {
		let result = try await hyperIdKYC.getUserKYCStatusTopLevelInfo(accessToken: accessToken)
		print(result)
	}
}
