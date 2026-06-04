package zzik2.zreflex.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class UnsafeAccess {

    private static final MethodHandles.Lookup TRUSTED_LOOKUP;
    private static final MethodHandle STATIC_FIELD_BASE;
    private static final MethodHandle STATIC_FIELD_OFFSET;
    private static final MethodHandle PUT_REFERENCE;

    static {
        try {
            TRUSTED_LOOKUP = bootstrapTrustedLookup();

            Class<?> internalUnsafe = Class.forName("jdk.internal.misc.Unsafe");
            MethodHandles.Lookup unsafeLookup = MethodHandles.privateLookupIn(internalUnsafe, TRUSTED_LOOKUP);
            Object unsafeInstance = unsafeLookup
                    .findStatic(internalUnsafe, "getUnsafe", MethodType.methodType(internalUnsafe))
                    .invoke();

            STATIC_FIELD_BASE = unsafeLookup
                    .findVirtual(internalUnsafe, "staticFieldBase", MethodType.methodType(Object.class, Field.class))
                    .bindTo(unsafeInstance);
            STATIC_FIELD_OFFSET = unsafeLookup
                    .findVirtual(internalUnsafe, "staticFieldOffset", MethodType.methodType(long.class, Field.class))
                    .bindTo(unsafeInstance);
            PUT_REFERENCE = unsafeLookup
                    .findVirtual(internalUnsafe, "putReference", MethodType.methodType(void.class, Object.class, long.class, Object.class))
                    .bindTo(unsafeInstance);
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static MethodHandles.Lookup bootstrapTrustedLookup() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        java.lang.reflect.Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
        java.lang.reflect.Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
        java.lang.reflect.Method getObject = unsafeClass.getMethod("getObject", Object.class, long.class);

        Object base = staticFieldBase.invoke(unsafe, implLookupField);
        long offset = (long) staticFieldOffset.invoke(unsafe, implLookupField);
        return (MethodHandles.Lookup) getObject.invoke(unsafe, base, offset);
    }

    private UnsafeAccess() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MethodHandles.Lookup trustedLookup() {
        return TRUSTED_LOOKUP;
    }

    public static MethodHandles.Lookup lookupFor(Class<?> clazz) {
        try {
            return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get lookup for class: " + clazz.getName(), e);
        }
    }

    public static void initializeClass(Class<?> clazz) {
        try {
            lookupFor(clazz).ensureInitialized(clazz);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize class: " + clazz.getName(), e);
        }
    }

    public static VarHandle unreflectVarHandle(Field field) {
        try {
            return lookupFor(field.getDeclaringClass()).unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to unreflect var handle for field: " + field, e);
        }
    }

    public static Object getStaticFieldValue(Field field) {
        return unreflectVarHandle(field).get();
    }

    public static void setStaticFieldValue(Field field, Object value) {
        if (Modifier.isFinal(field.getModifiers())) {
            setFinalStaticFieldValue(field, value);
        } else {
            unreflectVarHandle(field).set(value);
        }
    }

    private static void setFinalStaticFieldValue(Field field, Object value) {
        try {
            Object base = STATIC_FIELD_BASE.invoke(field);
            long offset = (long) STATIC_FIELD_OFFSET.invoke(field);
            PUT_REFERENCE.invoke(base, offset, value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to set final static field value: " + field, e);
        }
    }

    public static void setFieldValueVolatile(Object instance, Field field, Object value) {
        unreflectVarHandle(field).setVolatile(instance, value);
    }
}
