package com.mecong.restservice.service


import spock.lang.Specification

class TestEmailValidator extends Specification {
    EmailValidator emailValidator = new EmailValidator()

    def "Email validator should return true for valid emails"(String email, boolean isValid) {
        expect:
        emailValidator.emailValid(email) == isValid

        where:
        email                   | isValid
        "user1@comenon.com"     | true
        "user2@cherry.se"       | true
        "user3@not-so-coll.com" | false
        "invalid email.com"     | false
    }
}

