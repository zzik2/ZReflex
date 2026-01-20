package zzik2.zreflex.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * A plugin that changes the access modifiers of fields/methods of the target class after applying the mixin.
 * 
 * <p>
 * To use it, you need to register this plugin in your mixin.json:
 * </p>
 * 
 * <pre>
 * {
 *   "plugin": "zzik2.zreflex.mixin.ModifyAccessTransformer",
 *   ...
 * }
 * </pre>
 */
public class ModifyAccessTransformer implements IMixinConfigPlugin {

    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED;
    private static final String MODIFY_ACCESS_DESCRIPTOR = "L" + ModifyAccess.class.getName().replace('.', '/') + ";";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        processFields(targetClass);
        processMethods(targetClass);
    }

    private void processFields(ClassNode classNode) {
        for (FieldNode field : classNode.fields) {
            ModifyAccessInfo info = getModifyAccessInfo(field.visibleAnnotations, field.invisibleAnnotations);
            if (info != null) {
                field.access = applyAccessModifier(field.access, info);
            }
        }
    }

    private void processMethods(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ModifyAccessInfo info = getModifyAccessInfo(method.visibleAnnotations, method.invisibleAnnotations);
            if (info != null) {
                method.access = applyAccessModifier(method.access, info);
            }
        }
    }

    private ModifyAccessInfo getModifyAccessInfo(List<AnnotationNode> visibleAnnotations,
            List<AnnotationNode> invisibleAnnotations) {
        ModifyAccessInfo info = findAnnotation(visibleAnnotations);
        if (info != null)
            return info;
        return findAnnotation(invisibleAnnotations);
    }

    private ModifyAccessInfo findAnnotation(List<AnnotationNode> annotations) {
        if (annotations != null) {
            for (AnnotationNode node : annotations) {
                if (MODIFY_ACCESS_DESCRIPTOR.equals(node.desc)) {
                    return parseAnnotation(node);
                }
            }
        }
        return null;
    }

    private ModifyAccessInfo parseAnnotation(AnnotationNode node) {
        int access = 0;
        boolean removeFinal = false;
        if (node.values != null) {
            for (int i = 0; i < node.values.size(); i += 2) {
                String name = (String) node.values.get(i);
                Object value = node.values.get(i + 1);
                if ("access".equals(name)) {
                    if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        for (Object item : list) {
                            if (item instanceof Integer) {
                                access |= (Integer) item;
                            }
                        }
                    } else if (value instanceof Integer) {
                        access = (Integer) value;
                    }
                } else if ("removeFinal".equals(name)) {
                    removeFinal = (Boolean) value;
                }
            }
        }
        return new ModifyAccessInfo(access, removeFinal);
    }

    private int applyAccessModifier(int original, ModifyAccessInfo info) {
        int newAccess = original & ~ACCESS_MASK;
        newAccess |= (info.getAccess() & ACCESS_MASK);
        if (info.isRemoveFinal()) {
            newAccess &= ~Opcodes.ACC_FINAL;
        } else if ((info.getAccess() & Opcodes.ACC_FINAL) != 0) {
            newAccess |= Opcodes.ACC_FINAL;
        }
        return newAccess;
    }

    private static class ModifyAccessInfo {
        private final int access;
        private final boolean removeFinal;

        ModifyAccessInfo(int access, boolean removeFinal) {
            this.access = access;
            this.removeFinal = removeFinal;
        }

        int getAccess() {
            return access;
        }

        boolean isRemoveFinal() {
            return removeFinal;
        }
    }

}
