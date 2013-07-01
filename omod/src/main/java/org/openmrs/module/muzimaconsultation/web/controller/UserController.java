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
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
@Controller
public class UserController {

    @RequestMapping(value = "/module/muzimaconsultation/authenticated.json", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAuthenticatedUser() {
        User authenticatedUser = Context.getAuthenticatedUser();
        return convertUser(authenticatedUser);
    }

    @RequestMapping(value = "/module/muzimaconsultation/users.json", method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getAllUsers() {
        UserService userService = Context.getUserService();
        List<User> users = userService.getAllUsers();

        List<Object> objects = new ArrayList<Object>();
        for (User user : users) {
            objects.add(convertUser(user));
        }
        return objects;
    }

    //TODO: backend should return full name so it's searchable. Just need them for display and typeahead.
    private Map<String, Object> convertUser(final User authenticatedUser) {
        Map<String, Object> response = new HashMap<String, Object>();
        Person authenticatedPerson = authenticatedUser.getPerson();
        response.put("uuid", authenticatedPerson.getUuid());
        response.put("name", authenticatedPerson.getPersonName().getFullName());
        return response;
    }
}
