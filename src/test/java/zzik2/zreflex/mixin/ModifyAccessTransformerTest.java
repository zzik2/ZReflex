package zzik2.zreflex.mixin;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModifyAccessTransformerTest {

    // Test subclass to mock remapping behavior
    static class TestModifyAccessTransformer extends ModifyAccessTransformer {
        private final String remappedSuffix;

        TestModifyAccessTransformer() {
            this(null);
        }

        TestModifyAccessTransformer(String remappedSuffix) {
            this.remappedSuffix = remappedSuffix;
        }

        @Override
        protected String mapFieldName(String owner, String name, String desc) {
            if (remappedSuffix != null) {
                return name + remappedSuffix;
            }
            return name;
        }

        @Override
        protected String mapMethodName(String owner, String name, String desc) {
            if (remappedSuffix != null) {
                return name + remappedSuffix;
            }
            return name;
        }
    }

    @Test
    void testMethodRename() {
        ModifyAccessTransformer transformer = new TestModifyAccessTransformer();
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";

        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "oldName", "()V", null, null);

        // Add @ModifyName("newName") annotation
        AnnotationNode annotation = new AnnotationNode("Lzzik2/zreflex/mixin/ModifyName;");
        annotation.values = List.of("value", "newName");
        method.visibleAnnotations = List.of(annotation);

        classNode.methods.add(method);

        transformer.postApply("TestClass", classNode, "MixinClass", null);

        assertEquals("newName", method.name);
    }

    @Test
    void testFieldRename() {
        ModifyAccessTransformer transformer = new TestModifyAccessTransformer();
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";

        FieldNode field = new FieldNode(Opcodes.ACC_PRIVATE, "oldField", "I", null, null);

        // Add @ModifyName("newField") annotation
        AnnotationNode annotation = new AnnotationNode("Lzzik2/zreflex/mixin/ModifyName;");
        annotation.values = List.of("value", "newField");
        field.visibleAnnotations = List.of(annotation);

        classNode.fields.add(field);

        transformer.postApply("TestClass", classNode, "MixinClass", null);

        assertEquals("newField", field.name);
    }

    @Test
    void testRemapTrue() {
        // Mock remapper that adds "_remapped" suffix
        ModifyAccessTransformer transformer = new TestModifyAccessTransformer("_remapped");
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";

        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "oldName", "()V", null, null);

        // @ModifyName(value="newName", remap=true)
        AnnotationNode annotation = new AnnotationNode("Lzzik2/zreflex/mixin/ModifyName;");
        annotation.values = List.of("value", "newName", "remap", true);
        method.visibleAnnotations = List.of(annotation);

        classNode.methods.add(method);

        transformer.postApply("TestClass", classNode, "MixinClass", null);

        // Expect remapped name
        assertEquals("newName_remapped", method.name);
    }

    @Test
    void testRemapFalse() {
        // Mock remapper that adds "_remapped" suffix
        ModifyAccessTransformer transformer = new TestModifyAccessTransformer("_remapped");
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";

        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "oldName", "()V", null, null);

        // @ModifyName(value="newName", remap=false)
        AnnotationNode annotation = new AnnotationNode("Lzzik2/zreflex/mixin/ModifyName;");
        annotation.values = List.of("value", "newName", "remap", false);
        method.visibleAnnotations = List.of(annotation);

        classNode.methods.add(method);

        transformer.postApply("TestClass", classNode, "MixinClass", null);

        // Expect original newName, NOT remapped
        assertEquals("newName", method.name);
    }

    @Test
    void testNoChangeWithoutAnnotation() {
        ModifyAccessTransformer transformer = new TestModifyAccessTransformer();
        ClassNode classNode = new ClassNode();
        classNode.name = "TestClass";

        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "originalName", "()V", null, null);
        classNode.methods.add(method);

        transformer.postApply("TestClass", classNode, "MixinClass", null);

        assertEquals("originalName", method.name);
    }
}
