import Foundation

//**************************************************************************************************
//	MARK: MFATransactionStatus
//--------------------------------------------------------------------------------------------------
public enum MFATransactionStatus {
	case unsupported(code: Int64)
	
	case pending
	case completed(approved: MFACompleteResult)
	case expired
	case canceled
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(rawValue: Int64, completeResult: MFACompleteResult?) {
		switch rawValue {
		case 0:
			self = .pending
		case 1:
			self = .completed(approved: completeResult!)
		case 2:
			self = .expired
		case 4:
			self = .canceled
		default:
			self = .unsupported(code: rawValue)
		}
	}
}

//**************************************************************************************************
//	MARK: MFACompleteResult
//--------------------------------------------------------------------------------------------------
public enum MFACompleteResult {
	case unsupported(code: Int64)
	case approved
	case denied
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public init?(rawValue: Int64?) {
		guard let rawValue = rawValue else {
			return nil
		}
		switch rawValue {
		case 0:
			self = .approved
		case 1:
			self = .denied
		default:
			self = .unsupported(code: rawValue)
		}
	}
}
