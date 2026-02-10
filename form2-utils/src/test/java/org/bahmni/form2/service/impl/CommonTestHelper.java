/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.form2.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CommonTestHelper {
    public static void setValuesForMemberFields(Object classInstance, String fieldName, Object valueForMemberField)
            throws Exception {
        setField(classInstance, valueForMemberField, classInstance.getClass().getDeclaredField(fieldName));
    }

    public static void setValuesForSuperClassMemberFields(Object classInstance, String fieldName,
                                                          Object valueForMemberField) throws Exception {
        Field field = classInstance.getClass().getSuperclass().getDeclaredField(fieldName);
        setField(classInstance, valueForMemberField, field);
    }

    public static void setValuesForSuperSuperClassMemberFields(Object classInstance, String fieldName,
                                                               Object valueForMemberField) throws Exception {
        Field field = classInstance.getClass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
        setField(classInstance, valueForMemberField, field);
    }

    private static void setField(Object classInstance, Object valueForMemberField, Field field)
            throws IllegalAccessException {
        field.setAccessible(true);
        field.set(classInstance, valueForMemberField);
    }

    public static void setValueForFinalStaticField(Class classInstance, String fieldName, Object valueForMemberField)
            throws Exception {
        Field field = classInstance.getDeclaredField(fieldName);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        setField(null, valueForMemberField, field);
    }

    public static Method getPrivateMethod(Object classInstance, String methodName) throws NoSuchMethodException {
        Method declaredMethod = classInstance.getClass().getDeclaredMethod(methodName);
        declaredMethod.setAccessible(true);
        return declaredMethod;
    }
}

