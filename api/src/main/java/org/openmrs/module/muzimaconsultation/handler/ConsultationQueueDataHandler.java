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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.api.service.NotificationTokenService;
import org.openmrs.module.muzima.exception.QueueProcessorException;
import org.openmrs.module.muzima.model.NotificationData;
import org.openmrs.module.muzima.model.NotificationToken;
import org.openmrs.module.muzima.model.QueueData;
import org.openmrs.module.muzima.model.handler.QueueDataHandler;
import org.openmrs.module.muzima.web.resource.utils.JsonUtils;
import org.openmrs.module.muzima.model.MuzimaForm;
import org.openmrs.module.muzima.api.service.MuzimaFormService;
import org.openmrs.module.muzima.api.service.RegistrationDataService;
import org.openmrs.module.muzima.model.RegistrationData;
import org.openmrs.obs.ComplexData;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 */
@Component
@Handler(supports = QueueData.class, order = 50)
public class ConsultationQueueDataHandler implements QueueDataHandler {

    private static final String DISCRIMINATOR_VALUE = "json-consultation";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public final static String AUTH_KEY_FCM = "AAAAyR4Iero:APA91bFF5AB_mK-8xXVQQJXyT2pQRPhfkX99d7RAhbr0mLO9qbxNXEs8uOPHQ1r5zxUM4tzCGdEmzusBPYQchA4kJ_ewe-ZoGocHlTTDP4ylb7S7yk6l9ylcv3KYcAo0MKVZ91UNw_GSFTxwCTgR15PoffbDjgv7YA";

    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

    private static final String DEFAULT_ENCOUNTER_ROLE = "a0b03050-c99b-11e0-9572-0800200c9a66";

    private final Log log = LogFactory.getLog(ConsultationQueueDataHandler.class);

    private QueueProcessorException queueProcessorException;

    private Encounter encounter;

    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {

        try {
            if (validate(queueData)) {
                Context.getEncounterService().saveEncounter(encounter);

                try {
                    Role role = null;
                    Person recipient = null;
                    User user = null;
                    Object consultationObject = JsonUtils.readAsObject(queueData.getPayload(), "$['consultation']");
                    String recipientString = JsonUtils.readAsString(String.valueOf(consultationObject), "$['consultation.recipient']");
                    String sourceUuid = JsonUtils.readAsString(String.valueOf(consultationObject), "$['consultation.sourceUuid']");
                    String[] recipientParts = StringUtils.split(recipientString, ":");
                    if (ArrayUtils.getLength(recipientParts) == 2) {
                        if (StringUtils.equalsIgnoreCase(recipientParts[1], "u")) {
                            user = Context.getUserService().getUserByUsername(recipientParts[0]);
                            recipient = user.getPerson();
                        } else if (StringUtils.equalsIgnoreCase(recipientParts[1], "g")) {
                            role = Context.getUserService().getRole(recipientParts[0]);
                        }
                    }else{
                        user = Context.getUserService().getUserByUsername(recipientString);
                        recipient = user.getPerson();
                    }
                    generateNotification(sourceUuid, encounter, recipient, role, user);
                } catch (Exception e) {
                    if (!e.getClass().equals(QueueProcessorException.class)) {
                        String reason = "Unable to generate notification information. Rolling back encounter.";
                        queueProcessorException.addException(new Exception(reason, e));
                        Context.getEncounterService().voidEncounter(encounter, reason);
                    }
                }
            }
        } catch (Exception e) {
            if (!e.getClass().equals(QueueProcessorException.class))
                queueProcessorException.addException(e);
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    @Override
    public boolean validate(final QueueData queueData) throws QueueProcessorException {
        try {
            queueProcessorException = new QueueProcessorException();
            log.info("Processing encounter form data: " + queueData.getUuid());
            encounter = new Encounter();
            String payload = queueData.getPayload();

            //Object encounterObject = JsonUtils.readAsObject(queueData.getPayload(), "$['encounter']");
            processEncounter(encounter, payload);

            //Object patientObject = JsonUtils.readAsObject(queueData.getPayload(), "$['patient']");
            processPatient(encounter, payload);

            Object obsObject = JsonUtils.readAsObject(queueData.getPayload(), "$['observation']");
            processObs(encounter, null, obsObject);

            return true;

        } catch (Exception e) {
            queueProcessorException.addException(e);
            return false;
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    private void generateNotification(final String sourceUuid, final Encounter encounter, final Person recipient, final Role role, final User user) {
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
        notificationData.setPatient(patient);

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

        //Start of sending Notification
        String authKey = AUTH_KEY_FCM;
        String FMCurl = API_URL_FCM;

        URL url = null;
        try {
            url = new URL(FMCurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","key="+authKey);
            conn.setRequestProperty("Content-Type","application/json");

            JSONObject json = new JSONObject();
            NotificationTokenService notificationTokenService = Context.getService(NotificationTokenService.class);
            List<NotificationToken> notificationTokens = notificationTokenService.getNotificationByUserId(user);
            json.put("to",notificationTokens.get(0).getToken());
            JSONObject info = new JSONObject();
            info.put("title", "mUzima Consultation");
            info.put("body", "Hello "+user.getSystemId()+" you have a consultation pending your review");
            json.put("notification", info);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //End of sending Notification
    }

    private void processPatient(final Encounter encounter, final Object patientObject) {
        Patient unsavedPatient = new Patient();
        String patientPayload = patientObject.toString();

        String uuid = JsonUtils.readAsString(patientPayload, "$['patient']['patient.uuid']");
        unsavedPatient.setUuid(uuid);

        PatientService patientService = Context.getPatientService();
        LocationService locationService = Context.getLocationService();
        PatientIdentifierType defaultIdentifierType = patientService.getPatientIdentifierType(1);

        String identifier = JsonUtils.readAsString(patientPayload, "$['patient']['patient.medical_record_number']");
        String identifierTypeUuid = JsonUtils.readAsString(patientPayload, "$['patient']['patient.identifier_type']");
        String locationUuid = JsonUtils.readAsString(patientPayload, "$['patient']['patient.identifier_location']");

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        Location location = StringUtils.isNotBlank(locationUuid) ?
                locationService.getLocationByUuid(locationUuid) : encounter.getLocation();
        patientIdentifier.setLocation(location);
        PatientIdentifierType patientIdentifierType = StringUtils.isNotBlank(identifierTypeUuid) ?
                patientService.getPatientIdentifierTypeByUuid(identifierTypeUuid) : defaultIdentifierType;
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patientIdentifier.setIdentifier(identifier);
        unsavedPatient.addIdentifier(patientIdentifier);

        Date birthdate = JsonUtils.readAsDate(patientPayload, "$['patient']['patient.birth_date']");
        boolean birthdateEstimated = JsonUtils.readAsBoolean(patientPayload, "$['patient']['patient.birthdate_estimated']");
        String gender = JsonUtils.readAsString(patientPayload, "$['patient']['patient.sex']");

        unsavedPatient.setBirthdate(birthdate);
        unsavedPatient.setBirthdateEstimated(birthdateEstimated);
        unsavedPatient.setGender(gender);

        String givenName = JsonUtils.readAsString(patientPayload, "$['patient']['patient.given_name']");
        String middleName = JsonUtils.readAsString(patientPayload, "$['patient']['patient.middle_name']");
        String familyName = JsonUtils.readAsString(patientPayload, "$['patient']['patient.family_name']");

        PersonName personName = new PersonName();
        personName.setGivenName(givenName);
        personName.setMiddleName(middleName);
        personName.setFamilyName(familyName);

        unsavedPatient.addName(personName);
        unsavedPatient.addIdentifier(patientIdentifier);

        Patient candidatePatient;
        if (StringUtils.isNotEmpty(unsavedPatient.getUuid())) {
            candidatePatient = Context.getPatientService().getPatientByUuid(unsavedPatient.getUuid());
            if (candidatePatient == null) {
                String temporaryUuid = unsavedPatient.getUuid();
                RegistrationDataService dataService = Context.getService(RegistrationDataService.class);
                RegistrationData registrationData = dataService.getRegistrationDataByTemporaryUuid(temporaryUuid);
                if(registrationData!=null) {
                    candidatePatient = Context.getPatientService().getPatientByUuid(registrationData.getAssignedUuid());
                }
            }
        } else if (!StringUtils.isBlank(patientIdentifier.getIdentifier())) {
            List<Patient> patients = Context.getPatientService().getPatients(patientIdentifier.getIdentifier());
            candidatePatient = findPatient(patients, unsavedPatient);
        } else {
            List<Patient> patients = Context.getPatientService().getPatients(unsavedPatient.getPersonName().getFullName());
            candidatePatient = findPatient(patients, unsavedPatient);
        }

        if (candidatePatient == null) {
            queueProcessorException.addException(new Exception("Unable to uniquely identify patient for this encounter form data. "));
            //+ ToStringBuilder.reflectionToString(unsavedPatient)));
        } else {
            encounter.setPatient(candidatePatient);
        }
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

    private void processObs(final Encounter encounter, final Obs parentObs, final Object obsObject) {
        if (obsObject instanceof JSONObject) {
            JSONObject obsJsonObject = (JSONObject) obsObject;
            for (String conceptQuestion : obsJsonObject.keySet()) {
                String[] conceptElements = StringUtils.split(conceptQuestion, "\\^");
                if (conceptElements.length < 3)
                    continue;
                int conceptId = Integer.parseInt(conceptElements[0]);
                Concept concept = Context.getConceptService().getConcept(conceptId);
                if (concept == null) {
                    queueProcessorException.addException(new Exception("Unable to find Concept for Question with ID: " + conceptId));
                } else {
                    if (concept.isSet()) {
                        Obs obsGroup = new Obs();
                        obsGroup.setConcept(concept);
                        Object childObsObject = obsJsonObject.get(conceptQuestion);
                        processObsObject(encounter, obsGroup, childObsObject);
                        if (parentObs != null) {
                            parentObs.addGroupMember(obsGroup);
                        }
                    } else {
                        Object valueObject = obsJsonObject.get(conceptQuestion);
                        if (valueObject instanceof JSONArray) {
                            JSONArray jsonArray = (JSONArray) valueObject;
                            for (Object arrayElement : jsonArray) {
                                createObs(encounter, parentObs, concept, arrayElement);
                            }
                        } else {
                            createObs(encounter, parentObs, concept, valueObject);
                        }
                    }
                }
            }
        }else if(obsObject instanceof LinkedHashMap){
            Object obsAsJsonObject = new JSONObject((Map<String,?>)obsObject);
            processObs(encounter, parentObs, obsAsJsonObject);
        }
    }

    private void createObs(final Encounter encounter, final Obs parentObs, final Concept concept, final Object o) {
        String value=null;
        Obs obs = new Obs();
        obs.setConcept(concept);

        //check and parse if obs_value / obs_datetime object
        if(o instanceof LinkedHashMap){
            LinkedHashMap obj = (LinkedHashMap)o;
            if(obj.containsKey("obs_value")){
                value = (String)obj.get("obs_value");
            }
            if(obj.containsKey("obs_datetime")){
                String dateString = (String)obj.get("obs_datetime");
                Date obsDateTime = parseDate(dateString);
                obs.setObsDatetime(obsDateTime);
            }
        }else{
            value = o.toString();
        }
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
            if (valueCoded == null) {
                queueProcessorException.addException(new Exception("Unable to find concept for value coded with id: " + valueCodedId));
            } else {
                obs.setValueCoded(valueCoded);
            }
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
        //Object o = JsonUtils.readAsObject(childObsObject.toString(), "$");
        if (childObsObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) childObsObject;
            for (Object arrayElement : jsonArray) {
                Obs obsGroup = new Obs();
                obsGroup.setConcept(parentObs.getConcept());
                processObs(encounter, obsGroup, arrayElement);
                encounter.addObs(obsGroup);
            }
        } else if (childObsObject instanceof JSONObject) {
            processObs(encounter, parentObs, childObsObject);
            encounter.addObs(parentObs);
        }else if (childObsObject instanceof LinkedHashMap) {
            Object childObsAsJsonObject = new JSONObject((Map<String,?>)childObsObject);
            processObs(encounter, parentObs, childObsAsJsonObject);
            encounter.addObs(parentObs);
        }
    }

    private void processEncounter(final Encounter encounter, final Object encounterObject) throws QueueProcessorException {
        String encounterPayload = encounterObject.toString();

        String formUuid = JsonUtils.readAsString(encounterPayload, "$['encounter']['encounter.form_uuid']");
        Form form = Context.getFormService().getFormByUuid(formUuid);
        if (form == null) {
            MuzimaFormService muzimaFormService = Context.getService(MuzimaFormService.class);
            MuzimaForm muzimaForm = muzimaFormService.getFormByUuid(formUuid);
            if (muzimaForm != null) {
                Form formDefinition = Context.getFormService().getFormByUuid(muzimaForm.getForm());
                encounter.setForm(formDefinition);
                encounter.setEncounterType(formDefinition.getEncounterType());
            } else {
                log.info("Unable to find form using the uuid: " + formUuid + ". Setting the form field to null!");
                String encounterTypeString = JsonUtils.readAsString(encounterPayload, "$['encounter']['encounter.type_id']");
                int encounterTypeId = NumberUtils.toInt(encounterTypeString, -999);
                EncounterType encounterType = Context.getEncounterService().getEncounterType(encounterTypeId);
                if (encounterType == null) {
                    queueProcessorException.addException(new Exception("Unable to find encounter type using the id: " + encounterTypeString));
                } else {
                    encounter.setEncounterType(encounterType);
                }
            }
        } else {
            encounter.setForm(form);
            encounter.setEncounterType(form.getEncounterType());
        }

        String providerString = JsonUtils.readAsString(encounterPayload, "$['encounter']['encounter.provider_id']");
        User user = Context.getUserService().getUserByUsername(providerString);
        if (user == null) {
            queueProcessorException.addException(new Exception("Unable to find user using the id: " + providerString));
        } else {
              encounter.setCreator(user);
              Provider provider = Context.getProviderService().getProviderByIdentifier(providerString);
              String encounterRoleString = org.openmrs.module.muzima.utils.JsonUtils.readAsString(encounterPayload, "$['encounter']['encounter.provider_role_uuid']");
              EncounterRole encounterRole = null;

              if(StringUtils.isBlank(encounterRoleString)){
                  encounterRole = Context.getEncounterService().getEncounterRoleByUuid(DEFAULT_ENCOUNTER_ROLE);
              } else {
                  encounterRole = Context.getEncounterService().getEncounterRoleByUuid(encounterRoleString);
              }

                if(encounterRole == null){
                    queueProcessorException.addException(new Exception("Unable to find encounter role using the uuid: ["
                            + encounterRoleString + "] or the default role [" + DEFAULT_ENCOUNTER_ROLE +"]"));
                }
              encounter.setProvider(encounterRole,provider);
        }

        String locationString = JsonUtils.readAsString(encounterPayload, "$['encounter']['encounter.location_id']");
        int locationId = NumberUtils.toInt(locationString, -999);
        Location location = Context.getLocationService().getLocation(locationId);
        if (location == null) {
            queueProcessorException.addException(new Exception("Unable to find encounter location using the id: " + locationString));
        } else {
            encounter.setLocation(location);
        }

        Date encounterDatetime = JsonUtils.readAsDate(encounterPayload, "$['encounter']['encounter.encounter_datetime']");
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

    @Override
    public String getDiscriminator() {
        return DISCRIMINATOR_VALUE;
    }
}
