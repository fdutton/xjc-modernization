package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JDocComment;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.outline.ClassOutline;

public final class IntfAndImplMethodWriter extends BeanOnlyMethodWriter {
	private final JDefinedClass _interface;
	private JMethod signature; // TODO: One cannot declare two methods and then add an argument to the first method.

	public IntfAndImplMethodWriter(final ClassOutline target, final @NotNull Configuration configuration, final boolean requiredField, final boolean multiValuedField) {
		super(target, configuration, requiredField, multiValuedField);
		this._interface = target.ref;
	}

	public JVar addParameter(JType type, String name) {
		final JVar parameter = this.signature.param(type, name);
        if (!type.isPrimitive()) {
            for (final JClass annotation : isRequiredField() ? configuration().notNullableAnnotations(type.owner()) : configuration().nullableAnnotations(type.owner())) {
                parameter.annotate(annotation);
            }
        }
		return super.addParameter(type, name);
	}

	public JMethod declareMethod(JType returnType, String methodName) {
		this.signature = this._interface.method(NONE, returnType, methodName);
        if (!returnType.isPrimitive()) {
            for (final JClass annotation : isMultiValuedField() || isRequiredField() || configuration().useOptional() ? configuration().notNullableAnnotations(returnType.owner()) : configuration().nullableAnnotations(returnType.owner())) {
                this.signature.annotate(annotation);
            }
        }
		return super.declareMethod(returnType, methodName);
	}

	public JDocComment javadoc() {
		return this.signature.javadoc();
	}

}
