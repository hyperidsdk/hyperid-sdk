<?php

enum RoleCreateResult : int {
	case FAIL_BY_ROLE_LIMIT_REACHED				= -6;
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum RolesGetResult : int {
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum RoleDeleteResult : int {
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum UserRoleAttachResult : int {
	case FAIL_BY_USER_NOT_FOUND					= -7;
	case FAIL_BY_ROLE_NOT_FOUND					= -6;
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum UserRoleDetachResult : int {
	case FAIL_BY_USER_NOT_FOUND					= -7;
	case FAIL_BY_ROLE_NOT_FOUND					= -6;
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

enum UsersByRoleGetResult : int {
	case FAIL_BY_ROLE_NOT_FOUND					= -6;
	case FAIL_BY_INVALID_PARAMETERS				= -5;
	case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	= -4;
	case FAIL_BY_ACCESS_DENIED					= -3;
	case FAIL_BY_TOKEN_EXPIRED					= -2;
	case FAIL_BY_TOKEN_INVALID					= -1;
	case SUCCESS								= 0;
}

?>