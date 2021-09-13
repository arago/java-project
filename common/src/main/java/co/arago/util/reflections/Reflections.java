package co.arago.util.reflections;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Reflections {
    public static List<Field> getAllFields(Class<?> clazz) {
        if (clazz == null)
            return new ArrayList<>();

        List<Field> result = getAllFields(clazz.getSuperclass());
        result.addAll(List.of(clazz.getDeclaredFields()));
        return result;
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        if (clazz == null)
            return new ArrayList<>();

        List<Method> result = getAllMethods(clazz.getSuperclass());
        result.addAll(List.of(clazz.getDeclaredMethods()));
        return result;
    }

    public static Field findFieldByName(Class<?> clazz, String name) {
        if (clazz == null)
            return null;

        for (Field field : clazz.getDeclaredFields()) {
            if (StringUtils.equals(name, field.getName()))
                return field;
        }

        return findFieldByName(clazz.getSuperclass(), name);
    }

    public static Method findMethodByName(Class<?> clazz, String name) {
        if (clazz == null)
            return null;

        for (Method method : clazz.getDeclaredMethods()) {
            if (StringUtils.equals(name, method.getName()))
                return method;
        }

        return findMethodByName(clazz.getSuperclass(), name);
    }
}
