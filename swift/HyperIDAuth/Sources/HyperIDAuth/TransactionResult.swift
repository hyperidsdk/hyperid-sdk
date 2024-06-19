//==================================================================================================
//	TransactionResult
//--------------------------------------------------------------------------------------------------
enum TransactionResult : Int
{
	case success							= 0
	case failByServiceTemporaryUnavialable	= -1
	case failByInvalidParameters			= -2
	case rejectetByUser						= -3
	case failByCyberWallet					= -4
	case unrecognized						= -5
	
	//==================================================================================================
	//	init(rawValue: Int)
	//--------------------------------------------------------------------------------------------------
	init(rawValue: Int) {
		switch rawValue
		{
		case 0:
			self = .success
		case -1:
			self = .failByServiceTemporaryUnavialable
		case -2:
			self = .failByInvalidParameters
		case -3:
			self = .rejectetByUser
		case -4:
			self = .failByCyberWallet
		default:
			self = .unrecognized
		}
	}
}
