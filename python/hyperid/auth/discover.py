class Discover:
    def __init__(self, data):
        self.issuer = data.get('issuer')
        self.authorization_endpoint = data.get('authorization_endpoint')
        self.token_endpoint = data.get('token_endpoint')
        self.introspection_endpoint = data.get('introspection_endpoint')
        self.userinfo_endpoint = data.get('userinfo_endpoint')
        self.end_session_endpoint = data.get('end_session_endpoint')
        self.jwks_uri = data.get('jwks_uri')
        self.response_types_supported = data.get('response_types_supported', [])
        self.code_challenge_methods_supported = data.get('code_challenge_methods_supported', [])
        self.revocation_endpoint = data.get('revocation_endpoint')
        self.rest_api_token_endpoint = data.get('rest_api_token_endpoint')
        self.rest_api_public_endpoint = data.get('rest_api_public_endpoint')
        self.identity_providers = data.get('identity_providers', [])
        self.client_scopes_default = data.get('client_scopes_default', [])
        self.client_scopes_optional = data.get('client_scopes_optional', [])
        self.wallet_family = data.get('wallet_family', {})
        self.wallet_source = data.get('wallet_source', {})
        self.wallet_chain = data.get('wallet_chain', [])

    def get_scopes(self):
        combined_scopes = set(self.client_scopes_optional + self.client_scopes_default)
        return ' '.join(combined_scopes)