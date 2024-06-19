//==================================================================================================
//	MARK: Operation
//--------------------------------------------------------------------------------------------------
protocol Operation {
	var		id			: Int64	{ get }
	var		isCompleted	: Bool	{ get }
	
	func	process(accessToken: String) async throws
	func	fail(error: any Error)
	
}

//==================================================================================================
//	MARK: AuthorizedOperation
//--------------------------------------------------------------------------------------------------
class AuthorizedOperation<T> : Operation {
	var			id			: Int64	{ id_}
	var			isCompleted	: Bool = false
	
	let			id_			: Int64
	let			operation	: (_ accessToken	: String) async throws -> T
	let			onComplete	: (_ result			: T) -> ()
	let			onFail		: (_ error			: any Error) -> ()
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(id_			: Int64,
		 operation		: @escaping (_ accessToken	: String) async throws -> T,
		 onComplete		: @escaping (_ result		: T) -> (),
		 onFail			: @escaping (_ error		: any Error) -> ()) {
		self.id_ = id_
		self.operation = operation
		self.onComplete = onComplete
		self.onFail = onFail
	}
	//==================================================================================================
	//	process
	//--------------------------------------------------------------------------------------------------
	func process(accessToken	: String) async throws
	{
		do {
			try Task.checkCancellation()
			let result = try await operation(accessToken)
			if !isCompleted {
				onComplete(result)
				isCompleted = true
			}
		} catch HyperIDBaseAPIError.invalidAccessToken {
			throw HyperIDBaseAPIError.invalidAccessToken
		} catch {
			if !isCompleted {
				onFail(error)
				isCompleted = true
			}
		}
	}
	//==================================================================================================
	//	failAuthorizationExpired
	//--------------------------------------------------------------------------------------------------
	func fail(error : any Error) {
		if !isCompleted {
			onFail(error)
			isCompleted = true
		}
	}
}
