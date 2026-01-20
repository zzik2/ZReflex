package zzik2.zreflex.reflection;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

public final class ZReflectionTool {

    private ZReflectionTool() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> T getFieldValue(Object target, String fieldName) {
        return getFieldValue(target.getClass(), target, fieldName);
    }

    public static <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
        return getFieldValue(clazz, null, fieldName);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(Class<?> clazz, Object target, String fieldName) {
        try {
            Field field = findField(clazz, fieldName);
            ensureAccessible(field, fieldName);
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Failed to get field value: " + fieldName, e);
        }
    }

    public static void setFieldValue(Object target, String fieldName, Object value) {
        setFieldValue(target.getClass(), target, fieldName, value);
    }

    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        setFieldValue(clazz, null, fieldName, value);
    }

    private static void setFieldValue(Class<?> clazz, Object target, String fieldName, Object value) {
        try {
            Field field = findField(clazz, fieldName);
            ensureAccessible(field, fieldName);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Failed to set field value: " + fieldName, e);
        }
    }

    public static <T> T invokeMethod(Object target, String methodName, Object... args) {
        return invokeMethod(target.getClass(), target, methodName, null, args);
    }

    public static <T> T invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        return invokeMethod(target.getClass(), target, methodName, paramTypes, args);
    }

    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return invokeMethod(clazz, null, methodName, null, args);
    }

    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        return invokeMethod(clazz, null, methodName, paramTypes, args);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeMethod(Class<?> clazz, Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Class<?>[] resolvedTypes = paramTypes != null ? paramTypes : Arrays.stream(args).map(arg -> arg != null ? arg.getClass() : null).toArray(Class<?>[]::new);
            Method method = findMethod(clazz, methodName, resolvedTypes);
            ensureAccessible(method, methodName);
            return (T) method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Failed to invoke method: " + methodName, e);
        }
    }

    public static <T> T invokeMethodExact(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        return invokeMethodExact(target.getClass(), target, methodName, paramTypes, args);
    }

    public static <T> T invokeStaticMethodExact(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        return invokeMethodExact(clazz, null, methodName, paramTypes, args);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeMethodExact(Class<?> clazz, Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = findMethodExact(clazz, methodName, paramTypes);
            ensureAccessible(method, methodName);
            return (T) method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Failed to invoke method: " + methodName, e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) {
        return newInstance(clazz, null, args);
    }

    public static <T> T newInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        try {
            Class<?>[] resolvedTypes = paramTypes != null ? paramTypes
                    : Arrays.stream(args).map(arg -> arg != null ? arg.getClass() : null).toArray(Class<?>[]::new);
            Constructor<T> constructor = findConstructor(clazz, resolvedTypes);
            ensureAccessible(constructor, clazz.getName());
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Failed to create instance of: " + clazz.getName(), e);
        }
    }

    public static <T> T newInstanceExact(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
            ensureAccessible(constructor, clazz.getName());
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new ReflectionException("Failed to create instance of: " + clazz.getName(), e);
        }
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new ReflectionException("Field not found: " + fieldName + " in " + clazz.getName());
    }

    public static Optional<Field> findFieldOptional(Class<?> clazz, String fieldName) {
        try {
            return Optional.of(findField(clazz, fieldName));
        } catch (ReflectionException e) {
            return Optional.empty();
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && isAssignable(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        throw new ReflectionException("Method not found: " + methodName + " in " + clazz.getName());
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new ReflectionException("Method not found: " + methodName + " in " + clazz.getName());
    }

    public static Optional<Method> findMethodOptional(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return Optional.of(findMethod(clazz, methodName, paramTypes));
        } catch (ReflectionException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... paramTypes) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isAssignable(constructor.getParameterTypes(), paramTypes)) {
                return (Constructor<T>) constructor;
            }
        }
        throw new ReflectionException("Constructor not found in: " + clazz.getName());
    }

    public static <T> Optional<Constructor<T>> findConstructorOptional(Class<T> clazz, Class<?>... paramTypes) {
        try {
            return Optional.of(findConstructor(clazz, paramTypes));
        } catch (ReflectionException e) {
            return Optional.empty();
        }
    }

    public static int getFieldModifiers(Class<?> clazz, String fieldName) {
        return findField(clazz, fieldName).getModifiers();
    }

    public static int getMethodModifiers(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return findMethod(clazz, methodName, paramTypes).getModifiers();
    }

    public static int getConstructorModifiers(Class<?> clazz, Class<?>... paramTypes) {
        return findConstructor(clazz, paramTypes).getModifiers();
    }

    public static boolean isFieldPublic(Class<?> clazz, String fieldName) {
        return Modifier.isPublic(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldPrivate(Class<?> clazz, String fieldName) {
        return Modifier.isPrivate(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldProtected(Class<?> clazz, String fieldName) {
        return Modifier.isProtected(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldStatic(Class<?> clazz, String fieldName) {
        return Modifier.isStatic(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldFinal(Class<?> clazz, String fieldName) {
        return Modifier.isFinal(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldVolatile(Class<?> clazz, String fieldName) {
        return Modifier.isVolatile(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isFieldTransient(Class<?> clazz, String fieldName) {
        return Modifier.isTransient(getFieldModifiers(clazz, fieldName));
    }

    public static boolean isMethodPublic(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isPublic(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodPrivate(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isPrivate(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodProtected(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isProtected(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodStatic(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isStatic(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodFinal(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isFinal(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodAbstract(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isAbstract(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodSynchronized(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isSynchronized(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static boolean isMethodNative(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return Modifier.isNative(getMethodModifiers(clazz, methodName, paramTypes));
    }

    public static String modifiersToString(int modifiers) {
        return Modifier.toString(modifiers);
    }

    private static void ensureAccessible(AccessibleObject accessible, String name) {
        if (!accessible.trySetAccessible()) {
            throw new ReflectionException("Cannot access: " + name);
        }
    }

    private static boolean isAssignable(Class<?>[] declared, Class<?>[] provided) {
        if (declared.length != provided.length) {
            return false;
        }
        for (int i = 0; i < declared.length; i++) {
            if (provided[i] == null) {
                if (declared[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!isAssignableFrom(declared[i], provided[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignableFrom(Class<?> declared, Class<?> provided) {
        if (declared.isAssignableFrom(provided)) {
            return true;
        }
        if (declared.isPrimitive()) {
            return getWrapperType(declared).isAssignableFrom(provided);
        }
        if (provided.isPrimitive()) {
            return declared.isAssignableFrom(getWrapperType(provided));
        }
        return false;
    }

    private static Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class)
            return Integer.class;
        if (primitiveType == long.class)
            return Long.class;
        if (primitiveType == double.class)
            return Double.class;
        if (primitiveType == float.class)
            return Float.class;
        if (primitiveType == boolean.class)
            return Boolean.class;
        if (primitiveType == byte.class)
            return Byte.class;
        if (primitiveType == char.class)
            return Character.class;
        if (primitiveType == short.class)
            return Short.class;
        if (primitiveType == void.class)
            return Void.class;
        return primitiveType;
    }

    public static class ReflectionException extends RuntimeException {

        public ReflectionException(String message) {
            super(message);
        }

        public ReflectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}