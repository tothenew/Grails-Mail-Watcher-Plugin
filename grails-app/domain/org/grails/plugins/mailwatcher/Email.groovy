package org.grails.plugins.mailwatcher

import javax.mail.Message

class Email {
    String sender
    List<String> recipients
    List<String> ccRecipients
    List<String> bccRecipients
    String subject;
    String body;
    Date dateCreated
    Date lastUpdated
    Boolean isRead = false

    static constraints = {
        sender nullable: false, blank: false
        recipients nullable: false, validator: {List<String> val -> !val.isEmpty();}
        ccRecipients nullable: true
        bccRecipients nullable: true
        subject nullable: false, blank: false
        body nullable: false, blank: false
    }

    static hasMany = [recipients: String, ccRecipients: String, bccRecipients: String]

    static mapping = {
        table('mw_email')
        body type: 'text'
    }

    Email(Message message) {
        this.sender = message.from.toString()
        this.subject = message.subject
        this.body = message.inputStream.text
        this.recipients = message.getRecipients(Message.RecipientType.TO)?.toList()*.toString()
        this.ccRecipients = message.getRecipients(Message.RecipientType.CC)?.toList()*.toString()
        this.bccRecipients = message.getRecipients(Message.RecipientType.BCC)?.toList()*.toString()
    }
}
