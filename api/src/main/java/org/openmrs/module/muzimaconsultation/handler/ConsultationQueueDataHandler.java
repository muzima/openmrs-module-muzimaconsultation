/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.muzimaconsultation.handler;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.exception.QueueProcessorException;
import org.openmrs.module.muzima.model.NotificationData;
import org.openmrs.module.muzima.model.QueueData;
import org.openmrs.module.muzima.model.handler.QueueDataHandler;
import org.openmrs.module.muzimaconsultation.utils.JsonUtils;
import org.openmrs.obs.ComplexData;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 */
@Component
@Handler(supports = QueueData.class, order = 50)
public class ConsultationQueueDataHandler implements QueueDataHandler {

    private static final String DISCRIMINATOR_VALUE = "json-consultation";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Log log = LogFactory.getLog(ConsultationQueueDataHandler.class);

    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {
        log.info("Processing encounter form data: " + queueData.getUuid());
        if (!Context.isSessionOpen()){
            System.out.println("Session is not open");
            return;
        }
        Encounter encounter = new Encounter();

        Object encounterObject = JsonUtils.readAsObject(queueData.getPayload(), "$['encounter']");
        processEncounter(encounter, encounterObject);

        Object patientObject = JsonUtils.readAsObject(queueData.getPayload(), "$['patient']");
        processPatient(encounter, patientObject);

        Object obsObject = JsonUtils.readAsObject(queueData.getPayload(), "$['observation']");
        processObs(encounter, null, obsObject);
        Context.getEncounterService().saveEncounter(encounter);

        try {
            Role role = null;
            Person recipient = null;
            Object consultationObject = JsonUtils.readAsObject(queueData.getPayload(), "$['consultation']");
            String recipientString = JsonUtils.readAsString(String.valueOf(consultationObject), "$['consultation.recipient']");
            String sourceUuid = JsonUtils.readAsString(String.valueOf(consultationObject), "$['consultation.sourceUuid']");
            String[] recipientParts = StringUtils.split(recipientString, ":");
            if (ArrayUtils.getLength(recipientParts) == 2) {
                if (StringUtils.equalsIgnoreCase(recipientParts[1], "u")) {
                    User user = Context.getUserService().getUserByUsername(recipientParts[0]);
                    recipient = user.getPerson();
                } else if (StringUtils.equalsIgnoreCase(recipientParts[1], "g")) {
                    role = Context.getUserService().getRole(recipientParts[0]);
                }
            }
            generateNotification(sourceUuid, encounter, recipient, role);
        } catch (Exception e) {
            e.printStackTrace();
            String reason = "Unable to generate notification information. Rolling back encounter.";
            Context.getEncounterService().voidEncounter(encounter, reason);
            throw new QueueProcessorException(reason, e);
        }
    }

    private void generateNotification(final String sourceUuid, final Encounter encounter, final Person recipient, final Role role) {
        Person sender = encounter.getProvider();
        NotificationData notificationData = new NotificationData();
        notificationData.setRole(role);

        Patient patient = encounter.getPatient();
        String patientName = patient.getPersonName().getFullName();
        String senderName = sender.getPersonName().getFullName();
        String recipientName = "User";
        if (recipient != null) {
            recipientName = recipient.getPersonName().getFullName();
        } else if (role != null) {
            recipientName = role.getRole();
        }
        String subject = "New Consultation on " + patientName + " by " + senderName;
        notificationData.setSubject(subject);

        notificationData.setPayload("Dear " + recipientName + ",<br/>"
                + "<br/>Please review the newly created consultation request for the following patient:"
                + "<br/>Patient Name: " + patientName
                + "<br/>Requested Information: "
                + "<a href='/" + WebConstants.WEBAPP_NAME + "/admin/encounters/encounter.form?encounterId=" + encounter.getEncounterId() + "'>View Encounter</a>"
        );
        notificationData.setStatus("incoming");
        notificationData.setSource(sourceUuid);
        notificationData.setSender(sender);
        notificationData.setReceiver(recipient);
        Context.getService(DataService.class).saveNotificationData(notificationData);
    }

    private void processPatient(final Encounter encounter, final Object patientObject) throws QueueProcessorException {
        Patient unsavedPatient = new Patient();
        String patientPayload = patientObject.toString();

        String uuid = JsonUtils.readAsString(patientPayload, "$['patient.uuid']");
        unsavedPatient.setUuid(uuid);

        PatientService patientService = Context.getPatientService();
        LocationService locationService = Context.getLocationService();
        PatientIdentifierType defaultIdentifierType = patientService.getPatientIdentifierType(1);

        String identifier = JsonUtils.readAsString(patientPayload, "$['patient.medical_record_number']");
        String identifierTypeUuid = JsonUtils.readAsString(patientPayload, "$['patient.identifier_type']");
        String locationUuid = JsonUtils.readAsString(patientPayload, "$['patient.identifier_location']");

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        Location location = StringUtils.isNotBlank(locationUuid) ?
                locationService.getLocationByUuid(locationUuid) : encounter.getLocation();
        patientIdentifier.setLocation(location);
        PatientIdentifierType patientIdentifierType = StringUtils.isNotBlank(identifierTypeUuid) ?
                patientService.getPatientIdentifierTypeByUuid(identifierTypeUuid) : defaultIdentifierType;
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patientIdentifier.setIdentifier(identifier);
        unsavedPatient.addIdentifier(patientIdentifier);

        Date birthdate = JsonUtils.readAsDate(patientPayload, "$['patient.birthdate']");
        boolean birthdateEstimated = JsonUtils.readAsBoolean(patientPayload, "$['patient.birthdate_estimated']");
        String gender = JsonUtils.readAsString(patientPayload, "$['patient.sex']");

        unsavedPatient.setBirthdate(birthdate);
        unsavedPatient.setBirthdateEstimated(birthdateEstimated);
        unsavedPatient.setGender(gender);

        String givenName = JsonUtils.readAsString(patientPayload, "$['patient.given_name']");
        String middleName = JsonUtils.readAsString(patientPayload, "$['patient.middle_name']");
        String familyName = JsonUtils.readAsString(patientPayload, "$['patient.family_name']");

        PersonName personName = new PersonName();
        personName.setGivenName(givenName);
        personName.setMiddleName(middleName);
        personName.setFamilyName(familyName);

        unsavedPatient.addName(personName);
        unsavedPatient.addIdentifier(patientIdentifier);

        Patient candidatePatient;
        if (StringUtils.isNotEmpty(unsavedPatient.getUuid())) {
            candidatePatient = Context.getPatientService().getPatientByUuid(unsavedPatient.getUuid());
        } else if (!StringUtils.isBlank(patientIdentifier.getIdentifier())) {
            List<Patient> patients = Context.getPatientService().getPatients(patientIdentifier.getIdentifier());
            candidatePatient = findPatient(patients, unsavedPatient);
        } else {
            List<Patient> patients = Context.getPatientService().getPatients(unsavedPatient.getPersonName().getFullName());
            candidatePatient = findPatient(patients, unsavedPatient);
        }

        if (candidatePatient == null) {
            throw new QueueProcessorException("Unable to uniquely identify a patient for this encounter form data.");
        }

        encounter.setPatient(candidatePatient);
    }

    private Patient findPatient(final List<Patient> patients, final Patient unsavedPatient) {
        String unsavedGivenName = unsavedPatient.getGivenName();
        String unsavedFamilyName = unsavedPatient.getFamilyName();
        PersonName unsavedPersonName = unsavedPatient.getPersonName();
        for (Patient patient : patients) {
            // match it using the person name and gender, what about the dob?
            PersonName savedPersonName = patient.getPersonName();
            if (StringUtils.isNotBlank(savedPersonName.getFullName())
                    && StringUtils.isNotBlank(unsavedPersonName.getFullName())) {
                String savedGivenName = savedPersonName.getGivenName();
                int givenNameEditDistance = StringUtils.getLevenshteinDistance(
                        StringUtils.lowerCase(savedGivenName),
                        StringUtils.lowerCase(unsavedGivenName));
                String savedFamilyName = savedPersonName.getFamilyName();
                int familyNameEditDistance = StringUtils.getLevenshteinDistance(
                        StringUtils.lowerCase(savedFamilyName),
                        StringUtils.lowerCase(unsavedFamilyName));
                if (givenNameEditDistance < 3 && familyNameEditDistance < 3) {
                    if (StringUtils.equalsIgnoreCase(patient.getGender(), unsavedPatient.getGender())) {
                        if (patient.getBirthdate() != null && unsavedPatient.getBirthdate() != null
                                && DateUtils.isSameDay(patient.getBirthdate(), unsavedPatient.getBirthdate())) {
                            return patient;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void processObs(final Encounter encounter, final Obs parentObs, final Object obsObject) throws QueueProcessorException {
        if (obsObject instanceof JSONObject) {
            JSONObject obsJsonObject = (JSONObject) obsObject;
            for (String conceptQuestion : obsJsonObject.keySet()) {
                String[] conceptElements = StringUtils.split(conceptQuestion, "\\^");
                System.out.println("Question: " + conceptQuestion);
                if (conceptElements.length < 3)
                    continue;
                int conceptId = Integer.parseInt(conceptElements[0]);
                Concept concept = Context.getConceptService().getConcept(conceptId);
                if (concept.isSet()) {
                    Obs obsGroup = new Obs();
                    obsGroup.setConcept(concept);
                    processObsObject(encounter, obsGroup, obsJsonObject.get(conceptQuestion));
                    if (parentObs != null) {
                        parentObs.addGroupMember(obsGroup);
                    }
                } else {
                    Object valueObject = obsJsonObject.get(conceptQuestion);
                    Object o = JsonUtils.readAsObject(valueObject.toString(), "$");
                    if (o instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) o;
                        for (Object arrayElement : jsonArray) {
                            createObs(encounter, parentObs, concept, arrayElement);
                        }
                    } else {
                        createObs(encounter, parentObs, concept, valueObject);
                    }
                }
            }
        }
    }

    private void createObs(final Encounter encounter, final Obs parentObs, final Concept concept, final Object o) {
        String value = o.toString();
        Obs obs = new Obs();
        obs.setConcept(concept);
        // find the obs value :)
        if (concept.getDatatype().isNumeric()) {
            obs.setValueNumeric(Double.parseDouble(value));
        } else if (concept.getDatatype().isDate()
                || concept.getDatatype().isTime()
                || concept.getDatatype().isDateTime()) {
            obs.setValueDatetime(parseDate(value));
        } else if (concept.getDatatype().isCoded()) {
            String[] valueCodedElements = StringUtils.split(value, "\\^");
            int valueCodedId = Integer.parseInt(valueCodedElements[0]);
            Concept valueCoded = Context.getConceptService().getConcept(valueCodedId);
            obs.setValueCoded(valueCoded);
        } else if (concept.getDatatype().isText()) {
            obs.setValueText(value);
        } else if (concept.getDatatype().isComplex()) {
            String uniqueComplexName = UUID.randomUUID().toString();
            InputStream inputStream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(value));
            // TODO: this handler only assume jpg are coming for now. Need to add this extra information in the payload.
            ComplexData complexData = new ComplexData(uniqueComplexName + ".jpg", inputStream);
            obs.setComplexData(complexData);
            // see https://tickets.openmrs.org/browse/TRUNK-2582 for the fix version
            String handlerString = Context.getConceptService().getConceptComplex(obs.getConcept().getConceptId()).getHandler();
            Context.getObsService().getHandler(handlerString).saveObs(obs);
        }
        // only add if the value is not empty :)
        encounter.addObs(obs);
        if (parentObs != null) {
            parentObs.addGroupMember(obs);
        }
    }

    private void processObsObject(final Encounter encounter, final Obs parentObs, final Object childObsObject) {
        Object o = JsonUtils.readAsObject(childObsObject.toString(), "$");
        if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (Object arrayElement : jsonArray) {
                Obs obsGroup = new Obs();
                obsGroup.setConcept(parentObs.getConcept());
                processObs(encounter, obsGroup, arrayElement);
                encounter.addObs(obsGroup);
            }
        } else if (o instanceof JSONObject) {
            processObs(encounter, parentObs, o);
            encounter.addObs(parentObs);
        }
    }

    private void processEncounter(final Encounter encounter, final Object encounterObject) throws QueueProcessorException {
        String encounterPayload = encounterObject.toString();

        String formString = JsonUtils.readAsString(encounterPayload, "$['encounter.form_uuid']");
        Form form = Context.getFormService().getFormByUuid(formString);
        encounter.setForm(form != null ? form : Context.getFormService().getForm(-999));

        String encounterTypeString = JsonUtils.readAsString(encounterPayload, "$['encounter.type_id']");
        int encounterTypeId = NumberUtils.toInt(encounterTypeString, 1);
        EncounterType encounterType = Context.getEncounterService().getEncounterType(encounterTypeId);
        encounter.setEncounterType(encounterType);

        String providerString = JsonUtils.readAsString(encounterPayload, "$['encounter.provider_id']");
        User user = Context.getUserService().getUserByUsername(providerString);
        encounter.setProvider(user);

        String locationString = JsonUtils.readAsString(encounterPayload, "$['encounter.location_id']");
        int locationId = NumberUtils.toInt(locationString, -999);
        Location location = Context.getLocationService().getLocation(locationId);
        encounter.setLocation(location);

        Date encounterDatetime = JsonUtils.readAsDate(encounterPayload, "$['encounter.encounter_datetime']");
        encounter.setEncounterDatetime(encounterDatetime);
    }

    private Date parseDate(final String dateValue) {
        Date date = null;
        try {
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {
            log.error("Unable to parse date data for encounter!", e);
        }
        return date;
    }

    @Override
    public boolean accept(final QueueData queueData) {
        return StringUtils.equals(DISCRIMINATOR_VALUE, queueData.getDiscriminator());
    }
}
