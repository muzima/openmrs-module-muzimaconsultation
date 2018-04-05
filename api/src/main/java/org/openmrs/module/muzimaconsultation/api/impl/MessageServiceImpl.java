package org.openmrs.module.muzimaconsultation.api.impl;

import org.openmrs.Person;
import org.openmrs.module.muzimaconsultation.api.MessageService;
import org.openmrs.module.muzimaconsultation.api.db.MessageDAO;
import org.openmrs.module.muzimaconsultation.api.model.Message;

import java.util.List;

public class MessageServiceImpl implements MessageService{

    private MessageDAO messageDAO;

    public MessageServiceImpl(MessageDAO messageDAO){
        this.messageDAO = messageDAO;
    }

    @Override
    public void createMessage(Message message) {
        messageDAO.saveOrUpdate(message);
    }

    @Override
    public Message getByUuid(String uuid) {
        return getByUuid(uuid);
    }

    @Override
    public List<Message> getBySender(Person sender) {
        return messageDAO.getMessageBySender(sender);
    }

    @Override
    public List<Message> getByReceiver(Person receiver) {
        return messageDAO.getMessageByReceiver(receiver);
    }

    @Override
    public void voidMessage(String uuid) {
        messageDAO.voidMessage(uuid);
    }

    @Override
    public List<Message> getConversation(Person sender, Person receiver) {
        return getConversation(sender,receiver);
    }
}
