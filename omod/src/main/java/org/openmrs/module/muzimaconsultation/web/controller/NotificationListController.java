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
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.model.NotificationData;
import org.openmrs.module.muzimaconsultation.web.utils.NotificationDataConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Write brief description about the class here.
 */
@Controller
@RequestMapping(value = "module/muzimaconsultation/notifications.json")
public class NotificationListController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getNotificationsFor(final HttpServletRequest request) {
        List<Object> response = new ArrayList<Object>();

        String uuid = ServletRequestUtils.getStringParameter(request, "uuid", StringUtils.EMPTY);
        boolean sender = ServletRequestUtils.getBooleanParameter(request, "sender", false);

        Person person = Context.getPersonService().getPersonByUuid(uuid);
        DataService service = Context.getService(DataService.class);

        List<NotificationData> notificationDataList;
        if (sender) {
            notificationDataList = service.getAllNotificationDataFrom(person);
        } else {
            notificationDataList = service.getAllNotificationDataFor(person);
        }

        for (NotificationData notificationData : notificationDataList) {
            response.add(NotificationDataConverter.convert(notificationData));
        }

        return response;
    }
}
