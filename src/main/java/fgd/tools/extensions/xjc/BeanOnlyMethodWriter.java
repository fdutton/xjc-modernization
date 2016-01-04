package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JDocComment;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.outline.ClassOutline;

// TODO: Add @Override to methods when generating both interface and implementation.
public class BeanOnlyMethodWriter extends AugmentedMethodWriter {
	private final JDefinedClass implementation;
	private JMethod method; // TODO: One cannot declare two methods and then add an argument to the first method.

	public BeanOnlyMethodWriter(final ClassOutline target, final @NotNull Configuration configuration, final boolean requiredField, final boolean multiValuedField) {
		super(target, configuration, requiredField, multiValuedField);
		this.implementation = target.implClass;
	}

	@Override
	public JVar addParameter(JType type, String name) {
		final JVar var = this.method.param(type, name);
		var.mods().setFinal(true);
		if (!type.isPrimitive()) {
	        final JCodeModel cm = codeModel;
		    assert null != cm;
            for (final JClass annotation : isRequiredField() ? configuration().notNullableAnnotations(cm) : configuration().nullableAnnotations(cm)) {
                var.annotate(annotation);
            }
		}
		return var;
	}

	@Override
	public JMethod declareMethod(JType returnType, String methodName) {
        final int extendability = configuration().finalizeMethods() ? FINAL : NONE;
		this.method = this.implementation.method(PUBLIC | extendability, returnType, methodName);
        if (!returnType.isPrimitive()) {
            final JCodeModel cm = codeModel;
            assert null != cm;
            for (final JClass annotation : isMultiValuedField() || isRequiredField() || configuration().useOptional() ? configuration().notNullableAnnotations(cm) : configuration().nullableAnnotations(cm)) {
                this.method.annotate(annotation);
            }
        }
		return this.method;
	}

	@Override
	public JDocComment javadoc() {
		return this.method.javadoc();
	}

}
