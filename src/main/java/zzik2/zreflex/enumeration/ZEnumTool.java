package zzik2.zreflex.enumeration;

import zzik2.zreflex.internal.UnsafeAccess;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Utility class to dynamically add or create Enum constants at runtime.
 *
 * <p>
 * Note: This feature relies on JVM internals and is not guaranteed to work in all environments.
 * </p>
 */
public final class ZEnumTool {

    private static final String[] ENUM_VALUES_FIELD_NAMES = { "$VALUES", "ENUM$VALUES" };
    private static final long[] ENUM_CACHE_FIELD_OFFSETS;

    static {
        ENUM_CACHE_FIELD_OFFSETS = discoverEnumCacheOffsets();
    }

    private ZEnumTool() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <E extends Enum<E>> E addConstant(Class<E> enumType, String constantName) {
        return addConstant(enumType, constantName, new Class<?>[0]);
    }

    public static <E extends Enum<E>> E addConstant(Class<E> enumType, String constantName, Class<?>[] parameterTypes,
            Object... constructorArgs) {
        validateEnumType(enumType);
        validateConstantName(constantName);
        UnsafeAccess.initializeClass(enumType);

        E[] currentValues = getEnumValuesArray(enumType);
        int nextOrdinal = currentValues.length;

        E newConstant = createInstance(enumType, constantName, nextOrdinal, parameterTypes, constructorArgs);

        E[] expandedValues = expandArray(currentValues, newConstant);
        replaceEnumValuesArray(enumType, expandedValues);
        invalidateEnumCache(enumType);

        return newConstant;
    }

    public static <E extends Enum<E>> void addConstants(Class<E> enumType, Collection<E> constants) {
        validateEnumType(enumType);
        if (constants == null || constants.isEmpty()) {
            return;
        }
        UnsafeAccess.initializeClass(enumType);

        E[] currentValues = getEnumValuesArray(enumType);
        E[] expandedValues = expandArray(currentValues, constants);
        replaceEnumValuesArray(enumType, expandedValues);
        invalidateEnumCache(enumType);
    }

    public static <E extends Enum<E>> E createInstance(Class<E> enumType, String name, int ordinal) {
        return createInstance(enumType, name, ordinal, new Class<?>[0]);
    }

    public static <E extends Enum<E>> E createInstance(Class<E> enumType, String name, int ordinal,
            Class<?>[] parameterTypes, Object... constructorArgs) {
        validateEnumType(enumType);
        UnsafeAccess.initializeClass(enumType);

        try {
            List<Class<?>> fullParamTypes = buildConstructorParameterTypes(parameterTypes);
            MethodType constructorType = MethodType.methodType(void.class, fullParamTypes);
            MethodHandle constructorHandle = UnsafeAccess.trustedLookup().findConstructor(enumType, constructorType);

            List<Object> fullArgs = buildConstructorArguments(name, ordinal, constructorArgs);
            @SuppressWarnings("unchecked")
            E instance = (E) constructorHandle.invokeWithArguments(fullArgs);
            return instance;
        } catch (Throwable e) {
            throw new EnumException("Failed to create enum instance: " + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E[] getEnumValuesArray(Class<E> enumType) {
        Field valuesField = findValuesField(enumType);
        Object fieldBase = UnsafeAccess.getStaticFieldBase(valuesField);
        long fieldOffset = UnsafeAccess.getStaticFieldOffset(valuesField);
        return (E[]) UnsafeAccess.getObjectField(fieldBase, fieldOffset);
    }

    private static <E extends Enum<E>> void replaceEnumValuesArray(Class<E> enumType, E[] newValues) {
        Field valuesField = findValuesField(enumType);
        Object fieldBase = UnsafeAccess.getStaticFieldBase(valuesField);
        long fieldOffset = UnsafeAccess.getStaticFieldOffset(valuesField);
        UnsafeAccess.putObjectField(fieldBase, fieldOffset, newValues);
    }

    private static <E extends Enum<E>> Field findValuesField(Class<E> enumType) {
        for (String candidateName : ENUM_VALUES_FIELD_NAMES) {
            try {
                return enumType.getDeclaredField(candidateName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        for (Field field : enumType.getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType() == enumType
                    && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        }
        throw new EnumException("Cannot locate values field in enum: " + enumType.getName());
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E[] expandArray(E[] original, E newElement) {
        E[] expanded = (E[]) Array.newInstance(original.getClass().getComponentType(), original.length + 1);
        System.arraycopy(original, 0, expanded, 0, original.length);
        expanded[original.length] = newElement;
        return expanded;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E[] expandArray(E[] original, Collection<E> newElements) {
        E[] expanded = (E[]) Array.newInstance(original.getClass().getComponentType(),
                original.length + newElements.size());
        System.arraycopy(original, 0, expanded, 0, original.length);
        int index = original.length;
        for (E element : newElements) {
            expanded[index++] = element;
        }
        return expanded;
    }

    private static List<Class<?>> buildConstructorParameterTypes(Class<?>[] additionalTypes) {
        List<Class<?>> types = new ArrayList<>(2 + additionalTypes.length);
        types.add(String.class);
        types.add(int.class);
        types.addAll(Arrays.asList(additionalTypes));
        return types;
    }

    private static List<Object> buildConstructorArguments(String name, int ordinal, Object[] additionalArgs) {
        List<Object> args = new ArrayList<>(2 + additionalArgs.length);
        args.add(name);
        args.add(ordinal);
        args.addAll(Arrays.asList(additionalArgs));
        return args;
    }

    private static long[] discoverEnumCacheOffsets() {
        String[] cacheFieldCandidates = { "enumConstantDirectory", "enumConstants", "enumVars" };
        List<Long> foundOffsets = new ArrayList<>();

        for (String fieldName : cacheFieldCandidates) {
            try {
                Field cacheField = Class.class.getDeclaredField(fieldName);
                foundOffsets.add(UnsafeAccess.getInstanceFieldOffset(cacheField));
            } catch (NoSuchFieldException ignored) {
            }
        }

        if (foundOffsets.isEmpty()) {
            return new long[0];
        }

        long[] offsets = new long[foundOffsets.size()];
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = foundOffsets.get(i);
        }
        return offsets;
    }

    private static void invalidateEnumCache(Class<?> enumType) {
        for (long offset : ENUM_CACHE_FIELD_OFFSETS) {
            UnsafeAccess.putObjectFieldVolatile(enumType, offset, null);
        }
    }

    private static void validateEnumType(Class<?> enumType) {
        if (enumType == null) {
            throw new EnumException("Enum type cannot be null");
        }
        if (!enumType.isEnum()) {
            throw new EnumException("Class is not an enum: " + enumType.getName());
        }
    }

    private static void validateConstantName(String name) {
        if (name == null || name.isEmpty()) {
            throw new EnumException("Constant name cannot be null or empty");
        }
    }

    public static class EnumException extends RuntimeException {

        public EnumException(String message) {
            super(message);
        }

        public EnumException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
