package fgd.tools.extensions.xjc;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.sun.tools.internal.xjc.generator.bean.MethodWriter;
import com.sun.tools.internal.xjc.outline.ClassOutline;

public abstract class AugmentedMethodWriter extends MethodWriter {

    private final boolean multiValuedField;
    private final boolean requiredField;
    private final @NotNull Configuration configuration;

    protected AugmentedMethodWriter(final ClassOutline target, final @NotNull Configuration configuration, final boolean requiredField, final boolean multiValuedField) {
        super(target);
        this.configuration = Objects.requireNonNull(configuration);
        this.multiValuedField = multiValuedField;
        this.requiredField = requiredField;
    }

    public static AugmentedMethodWriter create(final ClassOutline target, final @NotNull Configuration configuration, final boolean requiredField, final boolean multiValuedField) {
        switch (target.parent().getModel().strategy) {
            case BEAN_ONLY:
                return new BeanOnlyMethodWriter(target, configuration, requiredField, multiValuedField);
            case INTF_AND_IMPL:
                return new IntfAndImplMethodWriter(target, configuration, requiredField, multiValuedField);
            default:
                throw new IllegalStateException("Unexpected ImplStructureStrategy value");
        }
    }

    public final @NotNull Configuration configuration() {
        return this.configuration;
    }

    public final boolean isMultiValuedField() {
        return this.multiValuedField;
    }

    public final boolean isRequiredField() {
        return this.requiredField;
    }

}
