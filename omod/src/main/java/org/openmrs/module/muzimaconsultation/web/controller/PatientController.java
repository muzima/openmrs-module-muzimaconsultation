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
import org.openmrs.User;
import org.openmrs.api.PatientService;
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
public class PatientController {

    @RequestMapping(value = "/module/muzimaconsultation/patients.json", method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getAllPatients(final @RequestParam(value = "param", required = false) String param) {
        /*if (StringUtils.isEmpty(param) || param.equalsIgnoreCase("undefined")) {
            return null;
        } else {
            if (param.length() < 3) {
                return null;
            }
        }*/
        String mparam ="Fel";
        if (Context.isAuthenticated()) {
            PatientService patientService = Context.getPatientService();
            //List<Patient> patients = patientService.getPatients(mparam, mparam, null,false);
            List<Patient> patients = patientService.getAllPatients();
            List<Object> objects = new ArrayList<Object>();
            for (Patient patient : patients) {
                objects.add(convertPatient(patient));
            }
            return objects;
        }
        return null;
    }

    private Map<String, Object> convertPatient(final Patient patient) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("uuid", patient.getUuid());
        response.put("name", patient.getPersonName().getFullName());
        return response;
    }
}