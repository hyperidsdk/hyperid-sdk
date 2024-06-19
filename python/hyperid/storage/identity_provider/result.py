class UserDataKeysByIDPGet:
    def __init__(self, response_json):
        self.keys_private = None
        self.keys_public = None
        self.parse_result(response_json)

    def parse_result(self, response_json):
        self.keys_private = response_json.get('keys_private')
        self.keys_public = response_json.get('keys_public')