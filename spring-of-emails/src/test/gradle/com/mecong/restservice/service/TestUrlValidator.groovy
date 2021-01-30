package com.mecong.restservice.service


import spock.lang.Specification

class TestUrlValidator extends Specification {
    UrlValidator urlValidator = new UrlValidator()

    def "test validate Url"(String url, boolean isValid) {

        expect:
        urlValidator.validateUrl(url) == isValid

        where:
        url                                   | isValid
        "http://test.com/new-xml"             | true
        "http://test.com/duplicate-emails"    | true
        "http://test.com/duplicate-emails"    | true
        "http://comenon.com/duplicate-emails" | true
        "http://cherry.se/duplicate-emails"   | true
        "wrong url"                           | false
    }
}