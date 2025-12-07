package com.pandora.enchants.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class for reflection operations
 */
public class ReflectionUtil {
    
    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.error("Failed to get field value: " + fieldName);
            e.printStackTrace();
            return null;
        }
    }
    
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.error("Failed to set field value: " + fieldName);
            e.printStackTrace();
        }
    }
    
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Logger.error("Failed to get method: " + methodName);
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            Logger.error("Failed to invoke method: " + method.getName());
            e.printStackTrace();
            return null;
        }
    }
}


