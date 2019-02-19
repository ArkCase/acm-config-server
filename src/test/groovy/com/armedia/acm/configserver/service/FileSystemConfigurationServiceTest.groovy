package com.armedia.acm.configserver.service

import spock.lang.Specification

class FileSystemConfigurationServiceTest extends Specification {

    FileSystemConfigurationService service

    def setup() {
        service = new FileSystemConfigurationService("acm-config-server-repo/arkcase-test.yaml")
    }

    def "should update properties"() {
        given:
        def properties = ["a": "yes", "b": 20]

        when:
        service.updateProperties(properties)

        then:
        true
    }
}