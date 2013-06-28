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

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzima.api.service.DataService;
import org.openmrs.module.muzima.model.NotificationData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Write brief description about the class here.
 */
@Controller
@RequestMapping(value = "module/muzimaconsultation/notifications.list")
public class NotificationListController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<NotificationData> getNotificationsFor(@RequestParam(value = "for", required = true) String uuid) {
        Person person = Context.getPersonService().getPersonByUuid(uuid);
        DataService service = Context.getService(DataService.class);
        return service.getAllNotificationDataFrom(person);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<NotificationData> getNotificationsFrom(@RequestParam(value = "from", required = true) String uuid) {
        Person person = Context.getPersonService().getPersonByUuid(uuid);
        DataService service = Context.getService(DataService.class);
        return service.getAllNotificationDataFor(person);
    }
}
