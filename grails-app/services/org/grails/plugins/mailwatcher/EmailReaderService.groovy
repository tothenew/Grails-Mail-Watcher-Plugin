package org.grails.plugins.mailwatcher

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import javax.mail.Folder
import javax.mail.Store
import javax.mail.Session
import javax.mail.Message
import javax.mail.search.FlagTerm
import javax.mail.Flags

class EmailReaderService {

    def getConfig() {
        return ConfigurationHolder.config
    }

    /** Mail Watcher Config Settings From App's Config */
    def getMailWatcherConfig() {
        return config.grails.mailwatcher
    }

    /**
     * If ReadTimeOut Specified In Config Then Use that or a minute as default
     * */
    Long getReadTimeOut() {
        return mailWatcherConfig.readTimeOut ?: 60000l //
    }

    /**
     * Reads Incoming Emails and Creates Db entries For Them
     * */
    void saveMailsToDB() {
        Folder folder = null
        try {
            folder = openMailFolder()
            setPermissionToReadAndWrite(folder)
            List<Message> messages = getUnreadMails(folder)
            saveMessages(messages)
        } catch (Exception e) {
            log.debug "${e.message}"
        } finally {
            folder?.store?.close()
        }
    }

    List<Email> saveMessages(List<Message> messages) {
        List<Email> mails = []
        messages.each {Message message ->
            printMessagesInfo(message)
            Email email = saveMessage(message)
            if (email) {
                mails.add(email)
            }
        }
        return mails
    }

    /**
     * Reads Message Objects and Saves Email object
     * */
    Email saveMessage(Message message) {
        Email email = new Email(message)
        return email.save(flush: true, failOnError: true)
    }

    /**
     * Logs Message Info, logging level set to Info
     * */
    void printMessagesInfo(Message message) {
        log.info "=============================================="
        log.info "From  ${message.from.toString()}"
        log.info "Recipients To ${message.getRecipients(Message.RecipientType.TO)}"
        log.info "Recipients CC ${message.getRecipients(Message.RecipientType.CC)}"
        log.info "Recipients BCC ${message.getRecipients(Message.RecipientType.BCC)}"
        log.info "Subject  ${message.subject}"
        log.info "=============================================="
    }


    Folder openMailFolder() {
        Store imapStore = mailSession.getStore(mailProtocol)
        connectImapStore(imapStore, mailHost)
        return imapStore.getFolder(mailFolderToRead);
    }

    /**
     * Sets Read Write Permission on Mail Folder
     * */
    void setPermissionToReadAndWrite(Folder folder) {
        folder.open(Folder.READ_WRITE)
    }

    /**
     * Reads Unread Mails From Specied Id
     * */
    List<Message> getUnreadMails(Folder folder) {
        FlagTerm flagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        List<Message> unread = folder.search(flagTerm);
        return unread
    }

    private connectImapStore(Store imapStore, String host) {
        if (!imapStore.isConnected()) {
            imapStore.connect(host, emailToRead, emailsPassword)
        }
    }

    Session getMailSession() {
        return Session.getDefaultInstance(setDefaultMailProperties(), null)
    }

    /**
     * Sets Properties Used For Mail Session
     * */
    private setDefaultMailProperties() {
        Properties props = new Properties()
        props.setProperty("mail.store.protocol", mailProtocol)
        props.setProperty("mail.imaps.host", mailHost)
        props.setProperty("mail.imaps.port", mailPort)
        return props
    }

    /**
     * reads email from Config 
     * */
    String getEmailToRead() {
        return mailWatcherConfig.email
    }

    /**
     * reads password from Config 
     * */
    String getEmailsPassword() {
        return mailWatcherConfig.password
    }

    /**
     * reads folder name from Config 
     * */
    String getMailFolderToRead() {
        return mailWatcherConfig.folderToRead

    }

    /**
     * reads protocol from Config 
     * */
    String getMailProtocol() {
        return mailWatcherConfig.protocol
    }

    /**
     * reads hostname from Config 
     * */
    String getMailHost() {
        return mailWatcherConfig.host
    }

    /**
     * reads port from Config 
     * */
    String getMailPort() {
        return mailWatcherConfig.port
    }


}                          
