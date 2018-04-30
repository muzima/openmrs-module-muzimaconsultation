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
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
            // TODO: Hack to get around bogus admin and daemon users! Bleh!!!
            if (!StringUtils.equalsIgnoreCase("admin", user.getSystemId())
                    && !StringUtils.equalsIgnoreCase("daemon", user.getSystemId())) {
                objects.add(convertUser(user));
            }
        }
        return objects;
    }

    //TODO: backend should return full name so it's searchable. Just need them for display and typeahead.
    private Map<String, Object> convertUser(final User user) {
        Map<String, Object> response = new HashMap<String, Object>();
        Person person = user.getPerson();
        response.put("uuid", person.getUuid());
        response.put("name", person.getPersonName().getFullName());
        return response;
    }

    @RequestMapping(value = "/module/muzimaconsultation/roles.json", method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getAllRoles() {
        User authenticatedUser = Context.getAuthenticatedUser();
        List<Object> objects = new ArrayList<Object>();
        for (Role role : authenticatedUser.getAllRoles()) {
            objects.add(converRole(role));
        }
        return objects;
    }

    private Map<String, Object> converRole(final Role role) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("uuid", role.getUuid());
        response.put("name", role.getRole());
        return response;
    }

    @RequestMapping(value = "/module/muzimaconsultation/providers.json", method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getAllProviders() {
        ProviderService providerService = Context.getProviderService();
        List<Provider> providers = providerService.getAllProviders();
        List<Object> objects = new ArrayList<Object>();
        for (Provider provider : providers) {
            objects.add(converProvider(provider));
        }
        return objects;
    }

    private Map<String, Object> converProvider(final Provider provider) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("uuid", provider.getUuid());
        response.put("name", provider.getName());
        return response;
    }
}
