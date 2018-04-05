package org.openmrs.module.muzimaconsultation.api;

import org.openmrs.Person;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.muzimaconsultation.api.model.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageService {

    /**
     * Create a new org.openmrs.module.muzimaconsultation.api.model.Message
     * @param message org.openmrs.module.muzimaconsultation.api.model.Message
     * Message param @should not be null.
     */
    @Transactional
    public void createMessage(Message message);

    /**
     * Obtains a message based on the message uuid.
     * @param uuid String
     * @return Message
     */
    public Message getByUuid(String uuid);

    /**
     * Obtain a java.util.List of messages based on the Sender as a filter.
     * @param sender Person
     * @return List type org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly = true)
    public List<Message> getBySender(Person sender);

    /**
     * Obtains a java.util.List of Messages based on the Receiver of the messages.Grants
     * messages that were sent to this particular receiver.
     * @should not be null.
     * @param receiver Person
     * @return List of org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly = true)
    public List<Message> getByReceiver(Person receiver);

    /**
     * Void a Message
     * @param uuid String
     */
    @Transactional(readOnly = false)
    public void voidMessage(String uuid);

    /**
     * Obtains the conversation (List of Messages) between defined sender and receiver.
     * @param sender Person
     * @param receiver Person
     * @return List of type org.openmrs.module.muzimaconsultation.api.model.Message
     */
    @Transactional(readOnly = true)
    public List<Message> getConversation(Person sender,Person receiver);

}
