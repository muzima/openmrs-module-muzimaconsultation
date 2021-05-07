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
package org.openmrs.module.muzimaconsultation.utils;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * TODO: Write brief description about the class here.
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class.getSimpleName());

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Write boolean value into the json object. The method will only write the boolean value if the object passed
     * as the first argument is an instance of <code>{@link net.minidev.json.JSONObject}</code>.
     *
     * @param object the <code>{@link net.minidev.json.JSONObject}</code> object
     * @param path   the path in the object.
     * @param value  the value to be assigned for the path.
     */
    public static void writeAsBoolean(final Object object, final String path, final boolean value) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.put(path, value);
        }
    }

    /**
     * Read boolean value from the json object.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the boolean value in the json object. When the path is invalid, by default will return false.
     */
    public static boolean readAsBoolean(final String jsonObject, final String path) {
        boolean returnedBoolean = false;
        try {
            returnedBoolean = (Boolean)JsonPath.read(jsonObject, path);
        } catch (Exception e) {
            logger.error("Unable to read boolean value with path: " + path);
        }
        return returnedBoolean;
    }

    /**
     * Write numeric value into the json object. The method will only write the numeric value if the object passed
     * as the first argument is an instance of <code>{@link net.minidev.json.JSONObject}</code>.
     *
     * @param object the <code>{@link net.minidev.json.JSONObject}</code> object
     * @param path   the path in the object.
     * @param value  the value to be assigned for the path.
     */
    public static void writeAsNumeric(final Object object, final String path, final double value) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.put(path, value);
        }
    }

    /**
     * Read numeric value from the json object.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the numeric value in the json object. When the path is invalid, by default will return 0.
     */
    public static double readAsNumeric(final String jsonObject, final String path) {
        double returnedString = 0;
        try {
            returnedString = (Double)JsonPath.read(jsonObject, path);
        } catch (Exception e) {
            logger.error("Unable to read string value with path: " + path);
        }
        return returnedString;
    }

    /**
     * Write string value into the json object. The method will only write the string value if the object passed
     * as the first argument is an instance of <code>{@link net.minidev.json.JSONObject}</code>.
     *
     * @param object the <code>{@link net.minidev.json.JSONObject}</code> object
     * @param path   the path in the object.
     * @param value  the value to be assigned for the path.
     */
    public static void writeAsString(final Object object, final String path, final String value) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.put(path, value);
        }
    }

    /**
     * Read string value from the json object.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the string value in the json object. When the path is invalid, by default will return null.
     */
    public static String readAsString(final String jsonObject, final String path) {
        String returnedString = null;
        try {
            returnedString = JsonPath.read(jsonObject, path);
        } catch (Exception e) {
            logger.error("Unable to read string value with path: " + path);
        }
        return returnedString;
    }

    /**
     * Write date value into the json object. The method will only write the date value if the object passed
     * as the first argument is an instance of <code>{@link net.minidev.json.JSONObject}</code>. Internally, the date will be
     * converted into string following the ISO-8601 format and write the value to the json object. If the date
     * is null, will write null value instead of empty string.
     *
     * @param object the <code>{@link net.minidev.json.JSONObject}</code> object
     * @param path   the path in the object.
     * @param value  the value to be assigned for the path.
     */
    public static void writeAsDateTime(final Object object, final String path, final Date value) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            String dateValue = null;
            if (value != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(value);
                dateValue = ISO8601Util.fromCalendar(calendar);
            }
            jsonObject.put(path, dateValue);
        }
    }

    /**
     * Read date value from the json object. The value for the path must conform to the ISO-8601 standard
     * date format.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the date value in the json object. When the path is invalid, by default will return null.
     * @see @link <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO-8601 Wikipedia Page</a>
     */
    public static Date readAsDateTime(final String jsonObject, final String path) {
        Date returnedDate = null;
        try {
            String dateAsString = readAsString(jsonObject, path);
            Calendar calendar = ISO8601Util.toCalendar(dateAsString);
            returnedDate = calendar.getTime();
        } catch (ParseException e) {
            logger.error("Unable to convert string value from path: " + path);
        } catch (Exception e) {
            logger.error("Unable to create date value from path: " + path);
        }
        return returnedDate;
    }

    /**
     * Read object value from the json object.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the object value in the json object. When the path is invalid, by default will return null.
     */
    public static Object readAsObject(final String jsonObject, final String path) {
        Object object = null;
        try {
            object = JsonPath.read(jsonObject, path);
        } catch (Exception e) {
            logger.error("Unable to read object value with path: " + path);
        }
        return object;
    }

    /**
     * Read list of object value from the json object.
     *
     * @param jsonObject the json object.
     * @param path       the path inside the json object.
     * @return the list of object values in the json object. When the path is invalid, by default will return empty list.
     */
    public static List<Object> readAsObjectList(final String jsonObject, final String path) {
        List<Object> objects = new ArrayList<Object>();
        try {
            objects = JsonPath.read(jsonObject, path);
        } catch (Exception e) {
            logger.error("Unable to read object value with path: " + path);
        }
        return objects;
    }

    /**
     * Write the day string of the date value into the json object. The method will only write the date value if the object passed
     * as the first argument is an instance of <code>{@link JSONObject}</code>. Internally, the date will be
     * converted into string of format yyyy-MM-dd without considering the timezone of the date. If the date
     * is null, will write null value instead of empty string.
     *
     * @param object the <code>{@link JSONObject}</code> object
     * @param path   the path in the object.
     * @param date  the value to be assigned for the path.
     */
    public static void writeAsDate(final Object object, final String path, final Date date) {
        if (object instanceof JSONObject && date != null) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.put(path, new SimpleDateFormat(DATE_PATTERN).format(date));
        }
    }

    /**
     * Read date value from the json object. The value for the path must conform to the "yyyy-MM-dd"
     * date format.
     *
     * @param serialized the serialized json object.
     * @param path       the path inside the json object.
     * @return the date value in the json object. When the path is invalid, by default will return null.
     */
    public static Date readAsDate(String serialized, String path) {
        String dateAsString = readAsString(serialized, path);
        if(dateAsString == null || dateAsString.length() == 0) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateAsString);
        } catch (ParseException e) {
            logger.error("Unable to convert string value from path: " + path);
        }
        return null;
    }
}
