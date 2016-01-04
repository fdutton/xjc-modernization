package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JExpr.*;
import static com.sun.codemodel.internal.JMod.*;
import static com.sun.codemodel.internal.JOp.*;
import static com.sun.tools.internal.xjc.outline.Aspect.*;
import static fgd.tools.extensions.xjc.XJCUtils.*;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldRef;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.generator.bean.BeanGenerator;
import com.sun.tools.internal.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.internal.xjc.generator.bean.field.Messages;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.outline.FieldOutline;
import com.sun.xml.internal.bind.api.impl.NameConverter;

public final class SingleValuedFieldRenderer extends AbstractFieldRenderer {

    public SingleValuedFieldRenderer(final @NotNull Configuration configuration) {
        super(configuration);
    }

    /**
     * Generates accesssors and fields for the given implementation class, then
     * return {@link FieldOutline} for accessing the generated field.
     */
    @Override
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo propertyInfo) {
        final BeanGenerator generator = context.parent();
        final JCodeModel codeModel = generator.getCodeModel();
        final Model model = generator.getModel();
        final JClass optional = codeModel.directClass("java.util.Optional");

        final JType implType = getType(context, propertyInfo, IMPLEMENTATION);
        final JType exposedType = getType(context, propertyInfo, EXPOSED);
        assert !exposedType.isPrimitive() && !implType.isPrimitive();

        final JFieldVar field = emitField(context, propertyInfo, implType, exposedType);
        final JFieldRef fieldRef = _this().ref(field);
        final boolean fieldIsRequired = isRequired(propertyInfo);

        final AugmentedMethodWriter writer = AugmentedMethodWriter.create(context, configuration(), fieldIsRequired, false);
        final NameConverter nc = model.getNameConverter();

        // [RESULT]
        // Type getXXX() {
        // #ifdef default value
        // if(value==null)
        // return defaultValue;
        // #endif
        // return value;
        // }
        JExpression defaultValue = null;
        if (propertyInfo.defaultValue != null) defaultValue = propertyInfo.defaultValue.compute(generator);

        // if Type is a wrapper and we have a default value,
        // we can use the primitive type.
        JType getterType = (model.options.enableIntrospection || null == defaultValue) ? exposedType : exposedType.unboxify();
        if (!fieldIsRequired && configuration().useOptional() && null == defaultValue) {
            getterType = optional.narrow(getterType.boxify());
        }

        JMethod getter = writer.declareMethod(getterType, getGetterMethod(context, propertyInfo, implType));

        String javadoc = propertyInfo.javadoc;
        if (javadoc.isEmpty()) {
            javadoc = Messages.DEFAULT_GETTER_JAVADOC.format(nc.toVariableName(propertyInfo.getName(true)));
        }
        writer.javadoc().append(javadoc);

        if (null != defaultValue) {
            getter.body()._return(cond(_null().ne(fieldRef), fieldRef, defaultValue));
        } else if (!fieldIsRequired && configuration().useOptional()) {
            getter.body()._return(optional.staticInvoke("ofNullable").arg(fieldRef));
        } else {
            getter.body()._return(fieldRef);
        }

        // There are two entries for each type: the name and a new line.
        final List<Object> possibleTypes = listPossibleTypes(generator, propertyInfo);

        final String documentation = XJCUtils.extractDocumentation(propertyInfo);
        if (documentation.isEmpty()) {
            if (2 < possibleTypes.size()) {
                writer.javadoc().addReturn().append("possible object is\n").append(possibleTypes);
            }
        } else {
            writer.javadoc().addReturn().append(documentation);
        }

        if (!fieldIsRequired) {
            // [RESULT]
            // void setXXX(Type newVal) {
            //     this.value = newVal;
            // }
            JMethod setter = writer.declareMethod(codeModel.VOID, "set" + propertyInfo.getName(true));
            JVar value = writer.addParameter(exposedType, "value");
            setter.body().assign(fieldRef, requireNotNull(codeModel, castToImplType(implType, exposedType, value), value.type().isReference() && fieldIsRequired));

            // setter always get the default javadoc. See issue #381
            writer.javadoc().append(Messages.DEFAULT_SETTER_JAVADOC.format(nc.toVariableName(propertyInfo.getName(true))));

            if (2 < possibleTypes.size()) {
                writer.javadoc().addParam(value).append("allowed object is\n").append(possibleTypes);
            }

            if (value.type().isReference() && fieldIsRequired) {
                writer.javadoc().addThrows(NullPointerException.class).append("if value is null");
            }
        }

        return new ModernizationFieldOutline(context, propertyInfo, field, exposedType);
    }

    /**
     * Case from {@link #exposedType} to {@link #implType} if necessary.
     */
    private final JExpression requireNotNull(final JCodeModel codeModel, final JExpression exp, final boolean required) {
        return !required ? exp : codeModel.ref(Objects.class).staticInvoke("requireNonNull").arg(exp).arg(lit("value cannot be null"));
    }

    /**
     * Case from {@link #exposedType} to {@link #implType} if necessary.
     */
    private final JExpression castToImplType(JType implType, JType exposedType, JExpression exp) {
        if (implType == exposedType)
            return exp;
        else
            return cast(implType, exp);
    }

    private final JFieldVar emitField(ClassOutlineImpl context, CPropertyInfo propertyInfo, JType implType, JType exposedType) {
        final boolean requiredField = isRequired(propertyInfo);
        final int visibility = configuration().privatizeFields() ? PRIVATE : PROTECTED;
        final JCodeModel model = context.parent().getCodeModel();

        JFieldVar field = context.implClass.field(visibility, implType, propertyInfo.getName(false));
        for (final JClass annotation : requiredField ? configuration().notNullableAnnotations(model) : configuration().nullableAnnotations(model)) {
            field.annotate(annotation);
        }
        annotate(context, propertyInfo, field, exposedType);
        return field;
    }

}