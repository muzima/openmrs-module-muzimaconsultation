package org.openmrs.module.muzimaconsultation.api.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsData;

public class Message extends BaseOpenmrsData {

    private int id;
    private String uuid;
    private String source;
    private String sender;
    private String receiver;
    private String subject;
    private String body;
    private String senderDate;
    private String senderTime;
    private Boolean voided;

    public Message(int id,String uuid, String source, String sender, String receiver, String subject, String body, String senderDate, String senderTime) {
        this.id = id;
        this.uuid = uuid;
        this.source = source;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
        this.senderDate = senderDate;
        this.senderTime = senderTime;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderDate() {
        return senderDate;
    }

    public void setSenderDate(String senderDate) {
        this.senderDate = senderDate;
    }

    public String getSenderTime() {
        return senderTime;
    }

    public void setSenderTime(String senderTime) {
        this.senderTime = senderTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", source='" + source + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", senderDate='" + senderDate + '\'' +
                ", senderTime='" + senderTime + '\'' +
                '}';
    }

    @Override
    public Boolean getVoided() {
        return voided;
    }

    @Override
    public void setVoided(Boolean voided) {
        this.voided = voided;
    }
}
