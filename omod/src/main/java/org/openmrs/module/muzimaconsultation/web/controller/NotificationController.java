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
package org.openmrs.module.muzimaconsultation.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.model.NotificationData;
import org.openmrs.module.muzimaconsultation.web.utils.NotificationDataConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
@Controller
@RequestMapping(value = "module/muzimaconsultation/notification.json")
public class NotificationController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getNotificationByUuid(final @RequestParam(required = true) String uuid) {
        DataService service = Context.getService(DataService.class);
        NotificationData notificationData = service.getNotificationDataByUuid(uuid);
        if (notificationData != null && notificationData.getStatus() != null){
            if (notificationData.getStatus().equalsIgnoreCase("incoming")){
                notificationData.setStatus("read");
                service.saveNotificationData(notificationData);
            }
        }
        return NotificationDataConverter.convert(service.getNotificationDataByUuid(uuid));
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(final @RequestBody Map<String, Object> request) throws IOException {
        if(Context.isAuthenticated()) {
            String recipientUuid = String.valueOf(request.get("recipient"));
            String roleUuid = String.valueOf(request.get("role"));
            String status = String.valueOf(request.get("status"));
            String source = String.valueOf(request.get("source"));
            String subject = String.valueOf(request.get("subject"));
            String payload = String.valueOf(request.get("payload"));
            String patientUuid = String.valueOf(request.get("patient"));

            DataService service = Context.getService(DataService.class);
            Person sender = Context.getAuthenticatedUser().getPerson();
            Person recipient = Context.getPersonService().getPersonByUuid(recipientUuid);
            Role role = Context.getUserService().getRoleByUuid(roleUuid);
            Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
            NotificationData notificationData = new NotificationData();
            notificationData.setRole(role);
            notificationData.setPayload(payload);
            notificationData.setSubject(subject);
            if (status == null || status.trim().length() < 1 || status.trim().equalsIgnoreCase("null")) {
                status = "unread";
            }
            notificationData.setStatus(status);
            notificationData.setSource(source);
            notificationData.setPatient(patient);
            notificationData.setSender(sender);
            notificationData.setReceiver(recipient);
            service.saveNotificationData(notificationData);
        }
    }
}
