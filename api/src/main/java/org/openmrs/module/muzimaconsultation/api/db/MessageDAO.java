package org.openmrs.module.muzimaconsultation.api.db;

import com.google.inject.ImplementedBy;
import org.openmrs.Person;
import org.openmrs.module.muzimaconsultation.api.db.hibernate.HibernateMessageDAOImpl;
import org.openmrs.module.muzimaconsultation.api.model.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD operations handler for Messages from db to service layer.
 */
@ImplementedBy(HibernateMessageDAOImpl.class)
public interface MessageDAO {

    /**
     * Create a new org.openmrs.module.muzimaconsultation.api.model.Message
     * @param message Message
     * Message param @should not be null.
     */
    @Transactional
    public void saveOrUpdate(Message message);

    /**
     * Obtains a message based on the message uuid passed as argument.
     * @param uuid String
     * @return org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly=true)
    public Message getMessageByUuid(String uuid);

    /**
     * Obtain a java.util.List of messages based on the Sender as a filter.
     * @param sender Person
     * @return List type org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly = true)
    public List<Message> getMessageBySender(Person sender);

    /**
     * Obtains a java.util.List of Messages based on the Receiver of the messages.Grants
     * messages that were sent to this particular receiver.
     * @should not be null.
     * @param receiver Person
     * @return List of org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly = true)
    public List<Message> getMessageByReceiver(Person receiver);

    /**
     * Void a Message
     * @param uuid String
     */
    public void voidMessage(String uuid);

    /**
     * Obtains the conversation (List of Messages) between defined sender and receiver.
     * @param sender Person
     * @param receiver Person
     * @return List of type org.openmrs.module.muzimaconsultation.api.model.Message
     */
    public List<Message>  getConversation(Person sender,Person receiver);
}
