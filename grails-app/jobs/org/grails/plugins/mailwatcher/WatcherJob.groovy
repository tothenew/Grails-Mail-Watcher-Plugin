package org.grails.plugins.mailwatcher

import org.codehaus.groovy.grails.commons.ConfigurationHolder


class WatcherJob {
    EmailReaderService emailReaderService

    def timeout = readTimeOut

    Long getReadTimeOut() {
        return ConfigurationHolder.config.grails.mailwatcher.readTimeOut ?: 60000
    }

    def execute() {
        emailReaderService.saveMailsToDB()
    }
}


