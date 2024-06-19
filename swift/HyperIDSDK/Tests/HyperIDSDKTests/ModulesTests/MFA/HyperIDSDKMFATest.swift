import XCTest
import HyperIDSDK

final class HyperIDSDKMFATest: XCTestCase {
	var hyperIdMFA		: HyperIDMFAAPI!
	var accessToken 	: String = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhMzQ3MzcyZS1mNjkwLTRiNmMtODQ4Yi0yY2I3NjM0NDdhNTMifQ.eyJleHAiOjE2OTk0NTA3MDYsImlhdCI6MTY5OTQ0NzEwNiwiYXV0aF90aW1lIjoxNjk5NDQ3MDkyLCJqdGkiOiJiNzQ4NGM3My04OGFkLTRlYjktYWZjMi1iMmIzY2E5MmNmODciLCJpc3MiOiJodHRwczovL2xvZ2luLXN0YWdlLmh5cGVyc2VjdXJlaWQuY29tL2F1dGgvcmVhbG1zL0h5cGVySUQiLCJzdWIiOiJhYmU0MzU4Ny05MWVkLTRkNzMtODEwZi03ZGIyNWRiMzU3MTEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhbmRyb2lkLXNkay10ZXN0Iiwic2Vzc2lvbl9zdGF0ZSI6IjczZjc4YTc4LTlkMTItNGYyZC1iY2NmLTRkNjAwZGY2M2Q2YiIsInJlZ2lvbl9pZCI6MCwic2NvcGUiOiJvcGVuaWQgdXNlci1kYXRhLXNldCBreWMtdmVyaWZpY2F0aW9uIGtleXMgdXNlci1pbmZvLWdldCBlbWFpbCBzZWNvbmQtZmFjdG9yLWF1dGgtY2xpZW50IGF1dGggbWZhLWNsaWVudCB1c2VyLWRhdGEtZ2V0IHdlYi1jaGF0Iiwic2lkIjoiNzNmNzhhNzgtOWQxMi00ZjJkLWJjY2YtNGQ2MDBkZjYzZDZiIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6IjE3OWVhOGNhYjE3MmRjOTg5NGNkZDU0NmRlNzM4MjdjOWExNGZlOTI0YWQ4Mzk3NjJmZDI1NTliNDYxZjQ0ZjAiLCJpcCI6IjMxLjEyOC4xNjIuODMiLCJkZXZpY2VfZGVzYyI6eyJjcHUiOnsiYXJjaGl0ZWN0dXJlIjoidW5rbm93biIsImNvcmVzIjo4fSwib3MiOnsidmVyc2lvbiI6Ik9TIFggMTBfMTVfNyIsInRpbWV6b25lIjotMTIwLCJ0eXBlIjoiRGVza3RvcCIsImRldmljZSI6eyJ2ZW5kb3IiOiJ1bmtub3duIiwibW9kZWwiOiJ1bmtub3duIn19LCJzY3JlZW4iOnsiZGVwdGgiOjI0LCJyYXRpbyI6MSwid2lkdGgiOjE5MjAsImhlaWdodCI6MTA4MCwic2l6ZSI6MjA3MzYwMH0sImJyb3dzZXIiOnsibmFtZSI6IlNhZmFyaSIsInZlcnNpb24iOiI2MDUuMS4xNSJ9LCJhZ2VudCI6Ik1vemlsbGEvNS4wIChNYWNpbnRvc2g7IEludGVsIE1hYyBPUyBYIDEwXzE1XzcpIEFwcGxlV2ViS2l0LzYwNS4xLjE1IChLSFRNTCwgbGlrZSBHZWNrbykgVmVyc2lvbi8xNy4xIFNhZmFyaS82MDUuMS4xNSJ9LCJlbWFpbCI6ImFuZHJpaS5idXRva0BsaXN0YXQuY29tLnVhIn0.eJpOVW_8FCrAnUhxvrNf3jmPonTzWkrGM-gnZrDOLiw"
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
				hyperIdMFA = try await HyperIDMFAAPI(providerInfo: ProviderInfo.stage)
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
	//	testCheckAvaibility
	//--------------------------------------------------------------------------------------------------
	func testCheckAvaibility() async throws {
		let result = try await hyperIdMFA.checkAvailability(accessToken: accessToken)
		print(result)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testStartMFATransaction() async throws {
		let transactionId = try await hyperIdMFA.startTransaction(question: "Will you marry pig?", controlCode: 23, accessToken: accessToken)
		print(transactionId)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testGetMFATransactionStatus() async throws {
		let status = try await hyperIdMFA.getTransactionStatus(transactionId: 8222, accessToken: accessToken)
		print(status!)
	}
	//==================================================================================================
	//	testStartMFATransaction
	//--------------------------------------------------------------------------------------------------
	func testCancelMFATransaction() async throws{
		try await hyperIdMFA.cancelTransaction(transactionId: 8225, accessToken: accessToken)
	}
}
