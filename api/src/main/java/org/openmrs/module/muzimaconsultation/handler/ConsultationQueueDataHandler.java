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

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.exception.QueueProcessorException;
import org.openmrs.module.muzima.model.NotificationData;
import org.openmrs.module.muzima.model.QueueData;
import org.openmrs.module.muzima.model.handler.QueueDataHandler;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 */
@Component
@Handler(supports = QueueData.class, order = 50)
public class ConsultationQueueDataHandler implements QueueDataHandler {

    private static final String DISCRIMINATOR_VALUE = "consultation";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Log log = LogFactory.getLog(ConsultationQueueDataHandler.class);

    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {
        log.info("Processing encounter form data: " + queueData.getUuid());
        String payload = queueData.getPayload();

        Encounter encounter = new Encounter();

        String personUuid = JsonPath.read(payload, "$['person']['person.uuid']");
        Patient patient = Context.getPatientService().getPatientByUuid(personUuid);
        encounter.setPatient(patient);

        String formUuid = JsonPath.read(payload, "$['encounter']['form.uuid']");
        Form form = Context.getFormService().getFormByUuid(formUuid);
        encounter.setForm(form);

        String encounterTypeUuid = JsonPath.read(payload, "$['encounter']['encounterType.uuid']");
        EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(encounterTypeUuid);
        encounter.setEncounterType(encounterType);

        String providerUuid = JsonPath.read(payload, "$['encounter']['provider.uuid']");
        User user = Context.getUserService().getUserByUuid(providerUuid);
        encounter.setProvider(user);

        String locationUuid = JsonPath.read(payload, "$['encounter']['location.uuid']");
        Location location = Context.getLocationService().getLocationByUuid(locationUuid);
        encounter.setLocation(location);

        String encounterDatetime = JsonPath.read(payload, "$['encounter']['datetime']");
        encounter.setEncounterDatetime(parseDate(encounterDatetime));

        List<Object> obsObjects = JsonPath.read(payload, "$['obs']");
        for (Object obsObject : obsObjects) {
            Obs obs = new Obs();

            String conceptUuid = JsonPath.read(obsObject, "$['uuid']");
            Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
            obs.setConcept(concept);

            String value = JsonPath.read(obsObject, "$['value']").toString();
            if (concept.getDatatype().isNumeric()) {
                obs.setValueNumeric(Double.parseDouble(value));
            } else if (concept.getDatatype().isDate()
                    || concept.getDatatype().isTime()
                    || concept.getDatatype().isDateTime()) {
                obs.setValueDatetime(parseDate(value));
            } else if (concept.getDatatype().isCoded()) {
                Concept valueCoded = Context.getConceptService().getConceptByUuid(value);
                obs.setValueCoded(valueCoded);
            } else if (concept.getDatatype().isText()) {
                obs.setValueText(value);
            } else if (concept.getDatatype().isComplex()) {
                byte[] bytes = Base64.decode(value);
                InputStream inputStream = new ByteArrayInputStream(bytes);
                ComplexData complexData = new ComplexData("ImageData", inputStream);
                obs.setComplexData(complexData);
            }
            encounter.addObs(obs);
        }

        Context.getEncounterService().saveEncounter(encounter);

        NotificationData notificationData = new NotificationData();
        Context.getService(DataService.class).saveNotificationData(notificationData);
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
