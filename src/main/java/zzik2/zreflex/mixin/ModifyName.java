package zzik2.zreflex.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Changes the name of the target field or method.
 *
 * <pre>
 * {@code
 * &#64;ModifyName("newName") @Shadow
 * private void oldName() {
 * }
 * }
 * </pre>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface ModifyName {

    /**
     * The new name to be applied.
     *
     * @return the new name
     */
    String value();

    /**
     * Whether to remap the name using the mixin remapper.
     *
     * @return true if remapping is enabled
     */
    boolean remap() default true;
}
