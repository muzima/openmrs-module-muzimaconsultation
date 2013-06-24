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
package org.openmrs.module.muzimaconsultation.api;

import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link ConsultationService}.
 */
public class ConsultationServiceTest extends BaseModuleContextSensitiveTest {

    private final Log log = LogFactory.getLog(ConsultationServiceTest.class);

    private static final String METHOD_POST = "POST";

    private static String registrationJson;

    private static String encounterJson;

    private static String consultationJson;

    @BeforeClass
    public static void prepare() throws Exception {
        JSONObject registrationObject = new JSONObject();
        registrationObject.put("dataSource", "da2a1c5a-04a6-4070-b2ca-81b57e1ab928");
        registrationObject.put("discriminator", "registration");
        JSONObject payloadObject = new JSONObject();
        payloadObject.put("patient.identifier", "9999-4");
        payloadObject.put("patient.identifier_type", "58a46e2e-1359-11df-a1f1-0026b9348838");
        payloadObject.put("patient.identifier_location", "c0937b97-1691-11df-97a5-7038c432aabf");
        payloadObject.put("patient.uuid", "6e698d66-9f59-4a3b-b3d7-91efb7b297d3");
        payloadObject.put("patient.birthdate", "1984-04-16 06:15:00");
        payloadObject.put("patient.birthdate_estimated", "false");
        payloadObject.put("patient.given_name", "Example");
        payloadObject.put("patient.middle_name", "of");
        payloadObject.put("patient.family_name", "Patient");
        payloadObject.put("patient.gender", "M");
        payloadObject.put("person_address.address1", "Adress 1");
        payloadObject.put("person_address.address2", "Adress 2");
        registrationObject.put("payload", payloadObject);
        registrationJson = registrationObject.toJSONString();
    }


    @Test
    public void shouldSetupContext() {
        assertNotNull(Context.getService(ConsultationService.class));
    }

    @Test
    public void postingQueueData() throws Exception {
        URL url = new URL("http://localhost:8081/openmrs-standalone/ws/rest/v1/muzima/queueData");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String encodedAuthorization = "Basic " + Base64.encode("admin:test".getBytes());
        connection.setRequestProperty("Authorization", encodedAuthorization);
        connection.setRequestMethod(METHOD_POST);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(registrationJson);
        writer.flush();

        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        int responseCode = reader.read();
        if (responseCode == HttpServletResponse.SC_OK
                || responseCode == HttpServletResponse.SC_CREATED) {
            log.info("Queue data created!");
        }

        reader.close();
        writer.close();
    }
}
