package zzik2.zreflex.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeAccessTest {

    static class UninitializedTarget {
        static final String INITIALIZED_VALUE = "initialized";
    }

    @Test
    @DisplayName("initializeClass should initialize a class without error")
    void initializeClassSucceeds() {
        assertDoesNotThrow(() -> UnsafeAccess.initializeClass(UninitializedTarget.class));
    }

    @Test
    @DisplayName("initializeClass result should make static fields accessible")
    void initializeClassMakesStaticFieldsAccessible() {
        UnsafeAccess.initializeClass(UninitializedTarget.class);
        assertEquals("initialized", UninitializedTarget.INITIALIZED_VALUE);
    }

    @Test
    @DisplayName("initializeClass should be idempotent")
    void initializeClassIdempotent() {
        UnsafeAccess.initializeClass(String.class);
        assertDoesNotThrow(() -> UnsafeAccess.initializeClass(String.class));
    }
}
