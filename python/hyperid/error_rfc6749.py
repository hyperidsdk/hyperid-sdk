class OAuth2Error(Exception):
    """Base class for OAuth2 related exceptions."""
    def __init__(self, description="An OAuth2 error occurred"):
        self.message = description
        super().__init__(description)

class InvalidRequest(OAuth2Error):
    def __init__(self):
        super().__init__("The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed.")

class UnauthorizedClient(OAuth2Error):
    def __init__(self):
        super().__init__("The client is not authorized to request an authorization code using this method.")

class AccessDenied(OAuth2Error):
    def __init__(self):
        super().__init__("The resource owner or authorization server denied the request.")

class UnsupportedResponseType(OAuth2Error):
    def __init__(self):
        super().__init__("The authorization server does not support obtaining an authorization code using this method.")

class InvalidScope(OAuth2Error):
    def __init__(self):
        super().__init__("The requested scope is invalid, unknown, or malformed.")
