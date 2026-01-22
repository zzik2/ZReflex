package zzik2.zreflex.internal;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class UnsafeAccess {

    private static final Object UNSAFE;
    private static final MethodHandles.Lookup TRUSTED_LOOKUP;

    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);

            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            TRUSTED_LOOKUP = (MethodHandles.Lookup) getObjectField(MethodHandles.Lookup.class, implLookup);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private UnsafeAccess() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MethodHandles.Lookup trustedLookup() {
        return TRUSTED_LOOKUP;
    }

    public static void initializeClass(Class<?> clazz) {
        try {
            java.lang.reflect.Method ensureInit = UNSAFE.getClass().getMethod("ensureClassInitialized", Class.class);
            ensureInit.invoke(UNSAFE, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize class: " + clazz.getName(), e);
        }
    }

    public static Object getStaticFieldBase(Field field) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("staticFieldBase", Field.class);
            return method.invoke(UNSAFE, field);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get static field base", e);
        }
    }

    public static long getStaticFieldOffset(Field field) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("staticFieldOffset", Field.class);
            return (long) method.invoke(UNSAFE, field);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get static field offset", e);
        }
    }

    public static long getInstanceFieldOffset(Field field) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("objectFieldOffset", Field.class);
            return (long) method.invoke(UNSAFE, field);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get instance field offset", e);
        }
    }

    public static Object getObjectField(Object base, long offset) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("getObject", Object.class, long.class);
            return method.invoke(UNSAFE, base, offset);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object field", e);
        }
    }

    public static Object getObjectField(Class<?> clazz, Field field) {
        return getObjectField(getStaticFieldBase(field), getStaticFieldOffset(field));
    }

    public static void putObjectField(Object base, long offset, Object value) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("putObject", Object.class, long.class,
                    Object.class);
            method.invoke(UNSAFE, base, offset, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to put object field", e);
        }
    }

    public static void putObjectFieldVolatile(Object base, long offset, Object value) {
        try {
            java.lang.reflect.Method method = UNSAFE.getClass().getMethod("putObjectVolatile", Object.class, long.class,
                    Object.class);
            method.invoke(UNSAFE, base, offset, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to put object field volatile", e);
        }
    }
}
