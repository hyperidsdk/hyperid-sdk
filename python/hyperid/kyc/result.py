from .enum import KycUserStatus, KycUserStatusTopLevelGetResult
from ..auth.enum import VerificationLevel
                            
class KycBase:
    def __init__(self, response_json):
        self.response_json = response_json
        self._parse_result()

    def _parse_result(self):
        raise NotImplementedError("Subclasses must implement this method.")
    
class KycUserStatusGet(KycBase):
    def __init__(self, response_json):
        self.verification_level = VerificationLevel.KYC_BASIC
        self.status = KycUserStatus.NONE
        self.applicant_id = None
        self.age = None
        self.country_a2 = None
        self.country_a3 = None
        self.provided_country_a2 = None
        self.provided_country_a3 = None
        self.address_country_a2 = None
        self.address_country_a3 = None
        self.phoneNumber_country_a2 = None
        self.phoneNumber_country_a3 = None
        self.phoneNumber_country_code = None
        self.ip_countries_a2 = None
        self.ip_countries_a3 = None
        self.moderation_comment = None
        self.reject_reasons = None
        self.support_link = None
        self.created_dt = None
        self.review_create_dt = None
        self.review_complete_dt = None
        self.expiration_dt = None
        super().__init__(response_json)

    def _parse_result(self):
        self.verification_level = VerificationLevel(self.response_json.get('verification_level'))
        self.status = KycUserStatus(self.response_json.get('user_status'))
        self.applicant_id = self.response_json.get('kyc_id')
        self.age = self.response_json.get('age')
        self.country_a2 = self.response_json.get('country_a2')
        self.country_a3 = self.response_json.get('country_a3')
        self.provided_country_a2 = self.response_json.get('provided_country_a2')
        self.provided_country_a3 = self.response_json.get('provided_country_a3')
        self.address_country_a2 = self.response_json.get('address_country_a2')
        self.address_country_a3 = self.response_json.get('address_country_a3')
        self.phoneNumber_country_a2 = self.response_json.get('phone_number_country_a2')
        self.phoneNumber_country_a3 = self.response_json.get('phone_number_country_a3')
        self.phoneNumber_country_code = self.response_json.get('phone_number_country_code')
        self.ip_countries_a2 = self.response_json.get('ip_countries_a2')
        self.ip_countries_a3 = self.response_json.get('ip_countries_a3')
        self.moderation_comment = self.response_json.get('moderation_comment')
        self.reject_reasons = self.response_json.get('reject_reasons')
        self.support_link = self.response_json.get('support_link')
        self.created_dt = self.response_json.get('create_dt')
        self.review_create_dt = self.response_json.get('review_create_dt')
        self.review_complete_dt = self.response_json.get('review_complete_dt')
        self.expiration_dt = self.response_json.get('expiration_dt')

class KycUserStatusTopLevelGet(KycBase):
    def __init__(self, response_json):
        self.verification_level = None
        self.status = None
        self.created_dt = None
        self.review_create_dt = None
        self.review_complete_dt = None
        super().__init__(response_json)

    def _parse_result(self):
        self.result = KycUserStatusTopLevelGetResult(self.response_json.get('result'))
        self.status = KycUserStatus(self.response_json.get('user_status'))
        self.verification_level = VerificationLevel(self.response_json.get('verification_level'))
        self.created_dt = self.response_json.get('create_dt')
        self.review_create_dt = self.response_json.get('review_create_dt')
        self.review_complete_dt = self.response_json.get('review_complete_dt')
