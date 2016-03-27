package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JExpr.*;
import static com.sun.codemodel.internal.JMod.*;
import static com.sun.codemodel.internal.JOp.*;
import static com.sun.tools.internal.xjc.outline.Aspect.*;
import static com.sun.xml.internal.xsom.XSFacet.*;
import static fgd.tools.extensions.xjc.XJCUtils.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;

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
import com.sun.xml.internal.xsom.XSAttributeDecl;
import com.sun.xml.internal.xsom.XSAttributeUse;
import com.sun.xml.internal.xsom.XSComponent;
import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSFacet;
import com.sun.xml.internal.xsom.XSParticle;
import com.sun.xml.internal.xsom.XSSimpleType;
import com.sun.xml.internal.xsom.XSTerm;
import com.sun.xml.internal.xsom.XSType;

public final class SingleValuedFieldRenderer extends AbstractFieldRenderer {

    public SingleValuedFieldRenderer(final @NotNull Configuration configuration) {
        super(configuration);
    }

    /**
     * Generates accessors and fields for the given implementation class, then
     * return {@link FieldOutline} for accessing the generated field.
     */
    @Override
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo propertyInfo) {
        final BeanGenerator generator = context.parent();
        final Model model = generator.getModel();
        final JCodeModel codeModel = generator.getCodeModel();
        final JClass optional = new JDirectClassEx(codeModel, "java.util.Optional");

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
        final JExpression defaultValue = null != propertyInfo.defaultValue ? propertyInfo.defaultValue.compute(generator) : null;

        // If exposedType is a wrapper and we have a default value we can use the primitive type.
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

        if (!configuration().finalizeFields() || !fieldIsRequired) {
            // [RESULT]
            // void setXXX(Type newVal) {
            //     this.value = newVal;
            // }
            JMethod setter = writer.declareMethod(generator.getCodeModel().VOID, "set" + propertyInfo.getName(true));

            // If exposedType is a wrapper and we have a default value we can use the primitive type.
            JType setterType = (model.options.enableIntrospection || null == defaultValue) ? exposedType : exposedType.unboxify();
            JVar value = writer.addParameter(setterType, "value");
            assert null != value;

            if (null != defaultValue && setterType.isPrimitive() && !fieldIsRequired) {
                setter.body().assign(fieldRef, cond(defaultValue.eq(value), _null(), setterType.boxify().staticInvoke("valueOf").arg(value)));
            } else {
                setter.body().assign(fieldRef, requireNotNull(codeModel, castToImplType(implType, exposedType, value), value.type().isReference() && fieldIsRequired));
            }

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
    private final JExpression castToImplType(final @NotNull JType implType, final @NotNull JType exposedType, final @NotNull JExpression exp) {
        if (Objects.equals(implType.fullName(), exposedType.fullName()))
            return exp;
        else
            return cast(implType, exp);
    }

    private final JFieldVar emitField(ClassOutlineImpl context, CPropertyInfo propertyInfo, JType implType, JType exposedType) {
        final boolean requiredField = isRequired(propertyInfo);
        final int visibility = 0
            | (requiredField && configuration().finalizeFields() ? FINAL : NONE)
            | (configuration().privatizeFields() ? PRIVATE : PROTECTED)
            ;

        JFieldVar field = context.implClass.field(visibility, implType, propertyInfo.getName(false));
        if (configuration().jsr303()) {
            // Add additional validation constraints based on any constraining facets present in the XML schema.
            final XSComponent schemaComponent = propertyInfo.getSchemaComponent();

            if (schemaComponent instanceof XSAttributeUse) {
                final XSAttributeUse attributeUsage = (XSAttributeUse) schemaComponent;
                final XSAttributeDecl attributeDeclaration = attributeUsage.getDecl();
                final XSSimpleType attributeType = attributeDeclaration.getType();
                addConstraints(field, attributeType);
            } else if (schemaComponent instanceof XSParticle) {
                final XSParticle particle = (XSParticle) schemaComponent;
                final XSTerm term = particle.getTerm();
                if (term instanceof XSElementDecl) {
                    final XSElementDecl elementDeclaration = (XSElementDecl) term;
                    final XSType elementType = elementDeclaration.getType();
                    if (elementType instanceof XSSimpleType) {
                        addConstraints(field, elementType.asSimpleType());
                    }
                }
            } else {
                System.currentTimeMillis();
            }
        }
        for (final JClass annotation : requiredField ? configuration().notNullableAnnotations(context.implClass.owner()) : configuration().nullableAnnotations(context.implClass.owner())) {
            field.annotate(annotation);
        }
        annotate(context, propertyInfo, field, exposedType);
        return field;
    }

    protected void addConstraints(JFieldVar field, final XSSimpleType simpleType) {
        // Ignore default facets
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(simpleType.getTargetNamespace())) return;

        final JCodeModel codeModel = field.type().owner();
        final XSSimpleType integerType = simpleType.getOwnerSchema().getSimpleType("integer");
        final boolean isInteger = simpleType.isDerivedFrom(integerType);

        final XSFacet length = simpleType.getFacet(FACET_LENGTH);
        if (null != length) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Size");
            final BigInteger value = DatatypeConverter.parseInteger(length.getValue().value);
            field.annotate(annotation).param("min", value.intValue()).param("max", value.intValue());
        }

        final XSFacet minLength = simpleType.getFacet(FACET_MINLENGTH);
        final XSFacet maxLength = simpleType.getFacet(FACET_MAXLENGTH);
        if (null != minLength && null != maxLength) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Size");
            final BigInteger min = DatatypeConverter.parseInteger(minLength.getValue().value);
            final BigInteger max = DatatypeConverter.parseInteger(maxLength.getValue().value);
            field.annotate(annotation).param("min", min.intValue()).param("max", max.intValue());
        } else if (null != minLength) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Size");
            final BigInteger min = DatatypeConverter.parseInteger(minLength.getValue().value);
            field.annotate(annotation).param("min", min.intValue());
        } else if (null != maxLength) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Size");
            final BigInteger max = DatatypeConverter.parseInteger(maxLength.getValue().value);
            field.annotate(annotation).param("max", max.intValue());
        }

        final XSFacet totalDigits = simpleType.getFacet(FACET_TOTALDIGITS);
        final XSFacet fractionDigits = simpleType.getFacet(FACET_FRACTIONDIGITS);
        if (null != totalDigits && null != fractionDigits) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Digits");
            final BigInteger integer = DatatypeConverter.parseInteger(totalDigits.getValue().value);
            final BigInteger fraction = DatatypeConverter.parseInteger(fractionDigits.getValue().value);
            field.annotate(annotation).param("integer", integer.intValue()).param("fraction", fraction.intValue());
        } else if (null != totalDigits) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Digits");
            final BigInteger integer = DatatypeConverter.parseInteger(totalDigits.getValue().value);
            field.annotate(annotation).param("integer", integer.intValue());
        } else if (null != fractionDigits && !isInteger) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Digits");
            final BigInteger fraction = DatatypeConverter.parseInteger(fractionDigits.getValue().value);
            field.annotate(annotation).param("fraction", fraction.intValue());
        }

        for (final XSFacet pattern : simpleType.getFacets(FACET_PATTERN)) {
            final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.Pattern");
            field.annotate(annotation).param("regexp", pattern.getValue().value);
        }

        final XSFacet minInclusive = simpleType.getFacet(FACET_MININCLUSIVE);
        if (null != minInclusive) {
            if (isInteger) {
                final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.DecimalMin");
                field.annotate(annotation).param("value", minInclusive.getValue().value).param("inclusive", true);
            } else {
                // TODO: Add support for non-integer based minInclusive.
            }
        }

        final XSFacet minExclusive = simpleType.getFacet(FACET_MINEXCLUSIVE);
        if (null != minExclusive) {
            if (isInteger) {
                final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.DecimalMin");
                field.annotate(annotation).param("value", minExclusive.getValue().value).param("inclusive", false);
            } else {
                // TODO: Add support for non-integer based minExclusive.
            }
        }

        final XSFacet maxInclusive = simpleType.getFacet(FACET_MAXINCLUSIVE);
        if (null != maxInclusive) {
            if (isInteger) {
                final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.DecimalMax");
                field.annotate(annotation).param("value", maxInclusive.getValue().value).param("inclusive", true);
            } else {
                // TODO: Add support for non-integer based maxInclusive.
            }
        }

        final XSFacet maxExclusive = simpleType.getFacet(FACET_MAXEXCLUSIVE);
        if (null != maxExclusive) {
            if (isInteger) {
                final JClass annotation = new JDirectClassEx(codeModel, "javax.validation.constraints.DecimalMax");
                field.annotate(annotation).param("value", maxExclusive.getValue().value).param("inclusive", false);
            } else {
                // TODO: Add support for non-integer based maxExclusive.
            }
        }
    }

}