package org.openmrs.module.muzimaconsultation.api.db.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.module.muzimaconsultation.api.db.MessageDAO;
import org.openmrs.module.muzimaconsultation.api.model.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HibernateMessageDAOImpl implements MessageDAO {

    private DbSession dbSession;

    public HibernateMessageDAOImpl(DbSession dbSession){
        this.dbSession = dbSession;
    }

    @Transactional
    @Override
    public void saveOrUpdate(Message message) {
        dbSession.saveOrUpdate(message);
    }

    @Transactional(readOnly = true)
    @Override
    public Message getMessageByUuid(String uuid) {
        Criteria criteria = dbSession.createCriteria(Message.class);
        criteria.add(Restrictions.eq("uuid",uuid));
        return (Message) Collections.singletonList(criteria.list()).get(0);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Message> getMessageBySender(Person sender) {
        Criteria criteria = dbSession.createCriteria(Message.class);
        criteria.add(Restrictions.eq("sender",sender));
        return criteria.list();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Message> getMessageByReceiver(Person receiver) {
        Criteria criteria = dbSession.createCriteria(Message.class);
        criteria.add(Restrictions.eq("receiver",receiver));
        return criteria.list();
    }

    @Transactional(readOnly = true)
    public void voidMessage(String uuid) {
        Criteria criteria = dbSession.createCriteria(Message.class);
        criteria.add(Restrictions.eq("uuid",uuid));
        Message matchedMessage  = (Message) criteria.list().get(0);
        matchedMessage.setVoided(true);
        saveOrUpdate(matchedMessage);
    }

    @Override
    public List<Message> getConversation(Person sender, Person receiver) {
        List<Message> messageList = new ArrayList<Message>();
        Criteria criteria = dbSession.createCriteria(Message.class);
        criteria.add(Restrictions.eq("receiver",receiver));
        criteria.add(Restrictions.eq("sender",sender));
        messageList = criteria.list();
        return messageList;
    }
}
