package zzik2.zreflex.enumeration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZEnumToolTest {

    enum SimpleColor {
        RED, GREEN, BLUE
    }

    enum SizedItem {
        SMALL(10),
        MEDIUM(20),
        LARGE(30);

        private final int size;

        SizedItem(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    @Test
    @DisplayName("Add simple enum constant without parameters")
    void addSimpleConstant() {
        SimpleColor yellow = ZEnumTool.addConstant(SimpleColor.class, "YELLOW");

        assertNotNull(yellow);
        assertEquals("YELLOW", yellow.name());
        assertEquals(3, yellow.ordinal());

        SimpleColor[] values = SimpleColor.values();
        assertEquals(4, values.length);
        assertSame(yellow, values[3]);
    }

    @Test
    @DisplayName("Add enum constant with constructor parameters")
    void addConstantWithParameters() {
        SizedItem extraLarge = ZEnumTool.addConstant(SizedItem.class, "EXTRA_LARGE", new Class<?>[] { int.class }, 50);

        assertNotNull(extraLarge);
        assertEquals("EXTRA_LARGE", extraLarge.name());
        assertEquals(50, extraLarge.getSize());

        SizedItem[] values = SizedItem.values();
        assertTrue(values.length >= 4);
    }

    @Test
    @DisplayName("Create enum instance without adding to values")
    void createInstanceOnly() {
        int initialLength = SimpleColor.values().length;

        SimpleColor temp = ZEnumTool.createInstance(SimpleColor.class, "TEMPORARY", 999);

        assertNotNull(temp);
        assertEquals("TEMPORARY", temp.name());
        assertEquals(999, temp.ordinal());
        assertEquals(initialLength, SimpleColor.values().length);
    }

    @Test
    @DisplayName("Throw exception for null enum type")
    void nullEnumType() {
        assertThrows(ZEnumTool.EnumException.class, () -> ZEnumTool.addConstant(null, "TEST"));
    }

    @Test
    @DisplayName("Throw exception for non-enum class")
    void nonEnumClass() {
        assertThrows(ZEnumTool.EnumException.class, () -> {
            @SuppressWarnings("unchecked")
            Class<Enum> fakeEnum = (Class<Enum>) (Class<?>) String.class;
            ZEnumTool.addConstant(fakeEnum, "TEST");
        });
    }

    @Test
    @DisplayName("Throw exception for null or empty constant name")
    void invalidConstantName() {
        assertThrows(ZEnumTool.EnumException.class, () -> ZEnumTool.addConstant(SimpleColor.class, null));
        assertThrows(ZEnumTool.EnumException.class, () -> ZEnumTool.addConstant(SimpleColor.class, ""));
    }
}
