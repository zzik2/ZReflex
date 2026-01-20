package zzik2.zreflex.reflection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import zzik2.zreflex.reflection.ZReflectionTool.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class ZReflectionToolTest {

    @Nested
    @DisplayName("Field Access Tests")
    class FieldAccessTests {

        @Test
        @DisplayName("Read instance private field")
        void getPrivateFieldValue() {
            TestClass target = new TestClass();
            String value = ZReflectionTool.getFieldValue(target, "privateField");
            assertEquals("private", value);
        }

        @Test
        @DisplayName("Read instance protected field")
        void getProtectedFieldValue() {
            TestClass target = new TestClass();
            String value = ZReflectionTool.getFieldValue(target, "protectedField");
            assertEquals("protected", value);
        }

        @Test
        @DisplayName("Read instance public field")
        void getPublicFieldValue() {
            TestClass target = new TestClass();
            String value = ZReflectionTool.getFieldValue(target, "publicField");
            assertEquals("public", value);
        }

        @Test
        @DisplayName("Read static field")
        void getStaticFieldValue() {
            String value = ZReflectionTool.getStaticFieldValue(TestClass.class, "staticField");
            assertEquals("static", value);
        }

        @Test
        @DisplayName("Write instance field")
        void setFieldValue() {
            TestClass target = new TestClass();
            ZReflectionTool.setFieldValue(target, "privateField", "modified");
            String value = ZReflectionTool.getFieldValue(target, "privateField");
            assertEquals("modified", value);
        }

        @Test
        @DisplayName("Write static field")
        void setStaticFieldValue() {
            String original = ZReflectionTool.getStaticFieldValue(TestClass.class, "staticField");
            try {
                ZReflectionTool.setStaticFieldValue(TestClass.class, "staticField", "modified");
                String value = ZReflectionTool.getStaticFieldValue(TestClass.class, "staticField");
                assertEquals("modified", value);
            } finally {
                ZReflectionTool.setStaticFieldValue(TestClass.class, "staticField", original);
            }
        }

        @Test
        @DisplayName("Access inherited field")
        void accessInheritedField() {
            ChildClass child = new ChildClass();
            String value = ZReflectionTool.getFieldValue(child, "parentField");
            assertEquals("parent", value);
        }

        @Test
        @DisplayName("Throw exception when accessing non-existent field")
        void nonExistentFieldThrowsException() {
            TestClass target = new TestClass();
            assertThrows(ReflectionException.class, () -> ZReflectionTool.getFieldValue(target, "nonExistent"));
        }

        @Test
        @DisplayName("Access field with null value")
        void nullFieldValue() {
            TestClass target = new TestClass();
            ZReflectionTool.setFieldValue(target, "privateField", null);
            String value = ZReflectionTool.getFieldValue(target, "privateField");
            assertNull(value);
        }

        @Test
        @DisplayName("Access primitive type field")
        void primitiveFieldAccess() {
            TestClass target = new TestClass();
            int value = ZReflectionTool.getFieldValue(target, "primitiveField");
            assertEquals(42, value);
            ZReflectionTool.setFieldValue(target, "primitiveField", 100);
            value = ZReflectionTool.getFieldValue(target, "primitiveField");
            assertEquals(100, value);
        }
    }

    @Nested
    @DisplayName("Method Invocation Tests")
    class MethodInvocationTests {

        @Test
        @DisplayName("Invoke no-arg method")
        void invokeNoArgMethod() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "noArgMethod");
            assertEquals("noArg", result);
        }

        @Test
        @DisplayName("Invoke method with args")
        void invokeMethodWithArgs() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "withArgs", "hello", 123);
            assertEquals("hello:123", result);
        }

        @Test
        @DisplayName("Invoke overloaded method with explicit types")
        void invokeOverloadedMethodWithExplicitTypes() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "overloaded", new Class<?>[] { String.class }, "test");
            assertEquals("string:test", result);
            result = ZReflectionTool.invokeMethod(target, "overloaded", new Class<?>[] { int.class }, 42);
            assertEquals("int:42", result);
        }

        @Test
        @DisplayName("Invoke static method")
        void invokeStaticMethod() {
            String result = ZReflectionTool.invokeStaticMethod(TestClass.class, "staticMethod");
            assertEquals("static", result);
        }

        @Test
        @DisplayName("Invoke static method with args")
        void invokeStaticMethodWithArgs() {
            int result = ZReflectionTool.invokeStaticMethod(TestClass.class, "staticAdd", 10, 20);
            assertEquals(30, result);
        }

        @Test
        @DisplayName("Invoke private method")
        void invokePrivateMethod() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "privateMethod");
            assertEquals("private", result);
        }

        @Test
        @DisplayName("Invoke inherited method")
        void invokeInheritedMethod() {
            ChildClass child = new ChildClass();
            String result = ZReflectionTool.invokeMethod(child, "parentMethod");
            assertEquals("parent", result);
        }

        @Test
        @DisplayName("Invoke void return method")
        void invokeVoidMethod() {
            TestClass target = new TestClass();
            Object result = ZReflectionTool.invokeMethod(target, "voidMethod");
            assertNull(result);
        }

        @Test
        @DisplayName("Throw exception when invoking non-existent method")
        void nonExistentMethodThrowsException() {
            TestClass target = new TestClass();
            assertThrows(ReflectionException.class, () -> ZReflectionTool.invokeMethod(target, "nonExistent"));
        }

        @Test
        @DisplayName("Invoke method with null arg")
        void invokeWithNullArg() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "acceptsNull", (Object) null);
            assertEquals("null", result);
        }

        @Test
        @DisplayName("Invoke with null arg and explicit type")
        void invokeWithNullArgExplicitType() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "acceptsNull", new Class<?>[] { String.class },
                    (Object) null);
            assertEquals("null", result);
        }

        @Test
        @DisplayName("invokeMethodExact - exact type matching")
        void invokeMethodExact() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethodExact(target, "overloaded", new Class<?>[] { int.class }, 42);
            assertEquals("int:42", result);
        }

        @Test
        @DisplayName("invokeStaticMethodExact - exact type matching")
        void invokeStaticMethodExact() {
            int result = ZReflectionTool.invokeStaticMethodExact(TestClass.class, "staticAdd",
                    new Class<?>[] { int.class, int.class }, 5, 7);
            assertEquals(12, result);
        }

        @Test
        @DisplayName("Primitive/wrapper compatibility")
        void primitiveWrapperCompatibility() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "takesInt", Integer.valueOf(10));
            assertEquals("10", result);
        }

        @Test
        @DisplayName("Invoke method with List arg")
        void invokeWithListArg() {
            TestClass target = new TestClass();
            List<String> list = Arrays.asList("a", "b", "c");
            int size = ZReflectionTool.invokeMethod(target, "getListSize", list);
            assertEquals(3, size);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Create instance with default constructor")
        void newInstanceDefaultConstructor() {
            TestClass instance = ZReflectionTool.newInstance(TestClass.class);
            assertNotNull(instance);
        }

        @Test
        @DisplayName("Create instance with args constructor")
        void newInstanceWithArgs() {
            TestClass instance = ZReflectionTool.newInstance(TestClass.class, "custom");
            assertEquals("custom", ZReflectionTool.getFieldValue(instance, "privateField"));
        }

        @Test
        @DisplayName("Create instance with private constructor")
        void newInstancePrivateConstructor() {
            PrivateConstructorClass instance = ZReflectionTool.newInstance(PrivateConstructorClass.class, "test");
            assertNotNull(instance);
        }

        @Test
        @DisplayName("Create instance with multiple args constructor")
        void newInstanceMultipleArgs() {
            TestClass instance = ZReflectionTool.newInstance(TestClass.class, "value", 123);
            assertEquals("value", ZReflectionTool.getFieldValue(instance, "privateField"));
            assertEquals(123, (int) ZReflectionTool.getFieldValue(instance, "primitiveField"));
        }

        @Test
        @DisplayName("Create instance with explicit types")
        void newInstanceWithExplicitTypes() {
            TestClass instance = ZReflectionTool.newInstance(TestClass.class,
                    new Class<?>[] { String.class, int.class }, "explicit", 999);
            assertEquals("explicit", ZReflectionTool.getFieldValue(instance, "privateField"));
            assertEquals(999, (int) ZReflectionTool.getFieldValue(instance, "primitiveField"));
        }

        @Test
        @DisplayName("newInstanceExact - exact type matching")
        void newInstanceExact() {
            TestClass instance = ZReflectionTool.newInstanceExact(TestClass.class, new Class<?>[] { String.class },
                    "exact");
            assertEquals("exact", ZReflectionTool.getFieldValue(instance, "privateField"));
        }

        @Test
        @DisplayName("Throw exception when no matching constructor")
        void noMatchingConstructorThrowsException() {
            assertThrows(ReflectionException.class, () -> ZReflectionTool.newInstance(TestClass.class, new Date()));
        }
    }

    @Nested
    @DisplayName("Find Method Tests")
    class FindTests {

        @Test
        @DisplayName("Find field")
        void findField() {
            Field field = ZReflectionTool.findField(TestClass.class, "privateField");
            assertNotNull(field);
            assertEquals("privateField", field.getName());
        }

        @Test
        @DisplayName("Find field Optional - exists")
        void findFieldOptionalExists() {
            Optional<Field> field = ZReflectionTool.findFieldOptional(TestClass.class, "privateField");
            assertTrue(field.isPresent());
        }

        @Test
        @DisplayName("Find field Optional - not exists")
        void findFieldOptionalNotExists() {
            Optional<Field> field = ZReflectionTool.findFieldOptional(TestClass.class, "nonExistent");
            assertFalse(field.isPresent());
        }

        @Test
        @DisplayName("Find method")
        void findMethod() {
            Method method = ZReflectionTool.findMethod(TestClass.class, "noArgMethod");
            assertNotNull(method);
        }

        @Test
        @DisplayName("Find method with param types")
        void findMethodWithParams() {
            Method method = ZReflectionTool.findMethod(TestClass.class, "withArgs", String.class, Integer.class);
            assertNotNull(method);
        }

        @Test
        @DisplayName("Find method Optional")
        void findMethodOptional() {
            Optional<Method> method = ZReflectionTool.findMethodOptional(TestClass.class, "nonExistent");
            assertFalse(method.isPresent());
        }

        @Test
        @DisplayName("Find method exact")
        void findMethodExact() {
            Method method = ZReflectionTool.findMethodExact(TestClass.class, "overloaded", int.class);
            assertNotNull(method);
        }

        @Test
        @DisplayName("Find constructor")
        void findConstructor() {
            Constructor<TestClass> constructor = ZReflectionTool.findConstructor(TestClass.class, String.class);
            assertNotNull(constructor);
        }

        @Test
        @DisplayName("Find constructor Optional")
        void findConstructorOptional() {
            Optional<Constructor<TestClass>> constructor = ZReflectionTool.findConstructorOptional(TestClass.class,
                    Date.class);
            assertFalse(constructor.isPresent());
        }
    }

    @Nested
    @DisplayName("Modifier Tests")
    class ModifierTests {

        @Test
        @DisplayName("Check field public")
        void isFieldPublic() {
            assertTrue(ZReflectionTool.isFieldPublic(TestClass.class, "publicField"));
            assertFalse(ZReflectionTool.isFieldPublic(TestClass.class, "privateField"));
        }

        @Test
        @DisplayName("Check field private")
        void isFieldPrivate() {
            assertTrue(ZReflectionTool.isFieldPrivate(TestClass.class, "privateField"));
            assertFalse(ZReflectionTool.isFieldPrivate(TestClass.class, "publicField"));
        }

        @Test
        @DisplayName("Check field protected")
        void isFieldProtected() {
            assertTrue(ZReflectionTool.isFieldProtected(TestClass.class, "protectedField"));
        }

        @Test
        @DisplayName("Check field static")
        void isFieldStatic() {
            assertTrue(ZReflectionTool.isFieldStatic(TestClass.class, "staticField"));
            assertFalse(ZReflectionTool.isFieldStatic(TestClass.class, "privateField"));
        }

        @Test
        @DisplayName("Check field final")
        void isFieldFinal() {
            assertTrue(ZReflectionTool.isFieldFinal(TestClass.class, "finalField"));
            assertFalse(ZReflectionTool.isFieldFinal(TestClass.class, "privateField"));
        }

        @Test
        @DisplayName("Check field volatile")
        void isFieldVolatile() {
            assertTrue(ZReflectionTool.isFieldVolatile(TestClass.class, "volatileField"));
        }

        @Test
        @DisplayName("Check field transient")
        void isFieldTransient() {
            assertTrue(ZReflectionTool.isFieldTransient(TestClass.class, "transientField"));
        }

        @Test
        @DisplayName("Check method public")
        void isMethodPublic() {
            assertTrue(ZReflectionTool.isMethodPublic(TestClass.class, "noArgMethod"));
        }

        @Test
        @DisplayName("Check method private")
        void isMethodPrivate() {
            assertTrue(ZReflectionTool.isMethodPrivate(TestClass.class, "privateMethod"));
        }

        @Test
        @DisplayName("Check method static")
        void isMethodStatic() {
            assertTrue(ZReflectionTool.isMethodStatic(TestClass.class, "staticMethod"));
        }

        @Test
        @DisplayName("Check method synchronized")
        void isMethodSynchronized() {
            assertTrue(ZReflectionTool.isMethodSynchronized(TestClass.class, "syncMethod"));
        }

        @Test
        @DisplayName("Check method final")
        void isMethodFinal() {
            assertTrue(ZReflectionTool.isMethodFinal(TestClass.class, "finalMethod"));
        }

        @Test
        @DisplayName("Modifiers to string")
        void modifiersToString() {
            int modifiers = ZReflectionTool.getFieldModifiers(TestClass.class, "staticField");
            String str = ZReflectionTool.modifiersToString(modifiers);
            assertTrue(str.contains("static"));
            assertTrue(str.contains("private"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Access field in deep inheritance")
        void deepInheritanceFieldAccess() {
            GrandChildClass grandChild = new GrandChildClass();
            String value = ZReflectionTool.getFieldValue(grandChild, "parentField");
            assertEquals("parent", value);
        }

        @Test
        @DisplayName("Invoke method in deep inheritance")
        void deepInheritanceMethodInvoke() {
            GrandChildClass grandChild = new GrandChildClass();
            String result = ZReflectionTool.invokeMethod(grandChild, "parentMethod");
            assertEquals("parent", result);
        }

        @Test
        @DisplayName("Access shadowed field in inheritance")
        void shadowedField() {
            ChildWithShadowedField child = new ChildWithShadowedField();
            String value = ZReflectionTool.getFieldValue(child, "shadowedField");
            assertEquals("child", value);
        }

        @Test
        @DisplayName("Access array type field")
        void arrayFieldAccess() {
            TestClass target = new TestClass();
            int[] array = ZReflectionTool.getFieldValue(target, "arrayField");
            assertArrayEquals(new int[] { 1, 2, 3 }, array);
        }

        @Test
        @DisplayName("Access generic field")
        void genericFieldAccess() {
            TestClass target = new TestClass();
            List<String> list = ZReflectionTool.getFieldValue(target, "genericField");
            assertNotNull(list);
        }

        @Test
        @DisplayName("Create inner class instance")
        void innerClassInstantiation() {
            TestClass outer = new TestClass();
            TestClass.InnerClass inner = ZReflectionTool.newInstance(TestClass.InnerClass.class, outer, "innerValue");
            assertNotNull(inner);
        }

        @Test
        @DisplayName("Invoke varargs method")
        void varargsMethodInvoke() {
            TestClass target = new TestClass();
            String result = ZReflectionTool.invokeMethod(target, "varArgsMethod",
                    new Object[] { new String[] { "a", "b", "c" } });
            assertEquals("3", result);
        }

        @Test
        @DisplayName("Method throws exception")
        void methodThrowsException() {
            TestClass target = new TestClass();
            ReflectionException ex = assertThrows(ReflectionException.class,
                    () -> ZReflectionTool.invokeMethod(target, "throwingMethod"));
            assertNotNull(ex.getCause());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    @Order(Integer.MAX_VALUE)
    class PerformanceTests {

        private static final int ITERATIONS = 100_000;
        private static final int WARMUP = 10_000;

        @Test
        @DisplayName("Field read performance")
        void fieldReadPerformance() {
            TestClass target = new TestClass();
            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.getFieldValue(target, "privateField");
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                ZReflectionTool.getFieldValue(target, "privateField");
            }
            long elapsed = System.nanoTime() - start;
            double avgNanos = (double) elapsed / ITERATIONS;
            System.out.printf("Field read: %.2f ns/op (total: %d ms)%n", avgNanos, elapsed / 1_000_000);
            assertTrue(avgNanos < 50_000, "Field read should be reasonably fast");
        }

        @Test
        @DisplayName("Field write performance")
        void fieldWritePerformance() {
            TestClass target = new TestClass();
            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.setFieldValue(target, "privateField", "value");
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                ZReflectionTool.setFieldValue(target, "privateField", "value");
            }
            long elapsed = System.nanoTime() - start;
            double avgNanos = (double) elapsed / ITERATIONS;
            System.out.printf("Field write: %.2f ns/op (total: %d ms)%n", avgNanos, elapsed / 1_000_000);
            assertTrue(avgNanos < 50_000, "Field write should be reasonably fast");
        }

        @Test
        @DisplayName("Method invoke performance")
        void methodInvokePerformance() {
            TestClass target = new TestClass();
            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.invokeMethod(target, "noArgMethod");
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                ZReflectionTool.invokeMethod(target, "noArgMethod");
            }
            long elapsed = System.nanoTime() - start;
            double avgNanos = (double) elapsed / ITERATIONS;
            System.out.printf("Method invoke: %.2f ns/op (total: %d ms)%n", avgNanos, elapsed / 1_000_000);
            assertTrue(avgNanos < 100_000, "Method invoke should be reasonably fast");
        }

        @Test
        @DisplayName("New instance performance")
        void newInstancePerformance() {
            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.newInstance(TestClass.class);
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                ZReflectionTool.newInstance(TestClass.class);
            }
            long elapsed = System.nanoTime() - start;
            double avgNanos = (double) elapsed / ITERATIONS;
            System.out.printf("New instance: %.2f ns/op (total: %d ms)%n", avgNanos, elapsed / 1_000_000);
            assertTrue(avgNanos < 100_000, "Instance creation should be reasonably fast");
        }

        @Test
        @DisplayName("Find field performance")
        void findFieldPerformance() {
            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.findField(TestClass.class, "privateField");
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                ZReflectionTool.findField(TestClass.class, "privateField");
            }
            long elapsed = System.nanoTime() - start;
            double avgNanos = (double) elapsed / ITERATIONS;
            System.out.printf("Find field: %.2f ns/op (total: %d ms)%n", avgNanos, elapsed / 1_000_000);
            assertTrue(avgNanos < 10_000, "Field lookup should be fast");
        }

        @Test
        @DisplayName("Direct access vs reflection comparison")
        void directVsReflectionComparison() {
            TestClass target = new TestClass();
            for (int i = 0; i < WARMUP; i++) {
                target.publicField.length();
            }
            long directStart = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String val = target.publicField;
            }
            long directElapsed = System.nanoTime() - directStart;

            for (int i = 0; i < WARMUP; i++) {
                ZReflectionTool.getFieldValue(target, "publicField");
            }
            long reflectionStart = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String val = ZReflectionTool.getFieldValue(target, "publicField");
            }
            long reflectionElapsed = System.nanoTime() - reflectionStart;

            double directAvg = (double) directElapsed / ITERATIONS;
            double reflectionAvg = (double) reflectionElapsed / ITERATIONS;
            double ratio = reflectionAvg / (directAvg == 0 ? 1 : directAvg);
            System.out.printf("Direct: %.2f ns/op, Reflection: %.2f ns/op, Ratio: %.1fx%n", directAvg, reflectionAvg,
                    ratio);
        }
    }

    static class TestClass {
        private String privateField = "private";
        protected String protectedField = "protected";
        public String publicField = "public";
        private static String staticField = "static";
        private final String finalField = "final";
        private volatile String volatileField = "volatile";
        private transient String transientField = "transient";
        private int primitiveField = 42;
        private int[] arrayField = { 1, 2, 3 };
        private List<String> genericField = new ArrayList<>();

        public TestClass() {
        }

        public TestClass(String value) {
            this.privateField = value;
        }

        public TestClass(String value, int primitiveValue) {
            this.privateField = value;
            this.primitiveField = primitiveValue;
        }

        public String noArgMethod() {
            return "noArg";
        }

        public String withArgs(String str, int num) {
            return str + ":" + num;
        }

        public String overloaded(String s) {
            return "string:" + s;
        }

        public String overloaded(int i) {
            return "int:" + i;
        }

        public static String staticMethod() {
            return "static";
        }

        public static int staticAdd(int a, int b) {
            return a + b;
        }

        private String privateMethod() {
            return "private";
        }

        public void voidMethod() {
        }

        public String acceptsNull(String s) {
            return s == null ? "null" : s;
        }

        public String takesInt(int value) {
            return String.valueOf(value);
        }

        public int getListSize(List<?> list) {
            return list.size();
        }

        public String varArgsMethod(String... args) {
            return String.valueOf(args.length);
        }

        public synchronized void syncMethod() {
        }

        public final void finalMethod() {
        }

        public void throwingMethod() {
            throw new RuntimeException("Test exception");
        }

        public class InnerClass {
            public InnerClass(String value) {
            }
        }
    }

    static class ParentClass {
        private String parentField = "parent";

        public String parentMethod() {
            return "parent";
        }
    }

    static class ChildClass extends ParentClass {
    }

    static class GrandChildClass extends ChildClass {
    }

    static class ChildWithShadowedField extends ParentClass {
        private String shadowedField = "child";
    }

    static class PrivateConstructorClass {
        private PrivateConstructorClass(String value) {
        }
    }
}
