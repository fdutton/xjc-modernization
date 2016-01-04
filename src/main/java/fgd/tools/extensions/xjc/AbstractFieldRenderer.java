package fgd.tools.extensions.xjc;

import static java.util.Objects.*;
import static com.sun.tools.internal.xjc.outline.Aspect.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import com.sun.codemodel.internal.JAnnotatable;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.api.SpecVersion;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlAnyElementWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlAttributeWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlElementRefWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlElementRefsWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlElementWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlElementsWriter;
import com.sun.tools.internal.xjc.generator.annotation.spec.XmlSchemaTypeWriter;
import com.sun.tools.internal.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.internal.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.internal.xjc.model.CAttributePropertyInfo;
import com.sun.tools.internal.xjc.model.CElement;
import com.sun.tools.internal.xjc.model.CElementInfo;
import com.sun.tools.internal.xjc.model.CElementPropertyInfo;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.model.CReferencePropertyInfo;
import com.sun.tools.internal.xjc.model.CTypeInfo;
import com.sun.tools.internal.xjc.model.CTypeRef;
import com.sun.tools.internal.xjc.model.CValuePropertyInfo;
import com.sun.tools.internal.xjc.model.nav.NClass;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.ClassOutline;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.tools.internal.xjc.reader.TypeUtil;
import com.sun.xml.internal.bind.api.impl.NameConverter;
import com.sun.xml.internal.bind.v2.TODO;

public abstract class AbstractFieldRenderer implements FieldRenderer {

    private final @NotNull Configuration configuration;
    private XmlElementsWriter xesw = null;

    protected AbstractFieldRenderer(final @NotNull Configuration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    /**
     * Gets the name of the getter method.
     *
     * <p>
     * This encapsulation is necessary because sometimes we use {@code isXXXX}
     * as the method name.
     */
    protected final String getGetterMethod(ClassOutline context, CPropertyInfo propertyInfo, JType type) {
        if (context.parent().getModel().options.enableIntrospection) {
            return ((type.isPrimitive() && type.boxify().getPrimitiveType() == context.parent().getCodeModel().BOOLEAN) ? "is" : "get") + propertyInfo.getName(true);
        } else {
            return (type.boxify().getPrimitiveType() == context.parent().getCodeModel().BOOLEAN ? "is" : "get") + propertyInfo.getName(true);
        }
    }

    private XmlElementWriter getXew(boolean checkWrapper, JAnnotatable field) {
        XmlElementWriter xew;
        if (checkWrapper) {
            if (xesw == null) {
                xesw = field.annotate2(XmlElementsWriter.class);
            }
            xew = xesw.value();
        } else {
            xew = field.annotate2(XmlElementWriter.class);
        }
        return xew;
    }

    /**
     * Annotate the field according to the recipes given as
     * {@link CPropertyInfo}.
     */
    protected final void annotate(ClassOutlineImpl context, CPropertyInfo propertyInfo, JAnnotatable field, JType exposedType) {
    
        assert (field != null);
    
        /*
         * TODO: consider moving this logic to somewhere else so that it can be
         * better shared, for how a field gets annotated doesn't really depend
         * on how we generate accessors.
         * 
         * so perhaps we should separate those two.
         */
    
        // TODO: consider a visitor
        if (propertyInfo instanceof CAttributePropertyInfo) {
            annotateAttribute(context, propertyInfo, field);
        } else if (propertyInfo instanceof CElementPropertyInfo) {
            annotateElement(context, propertyInfo, field, exposedType);
        } else if (propertyInfo instanceof CValuePropertyInfo) {
            field.annotate(XmlValue.class);
        } else if (propertyInfo instanceof CReferencePropertyInfo) {
            annotateReference(context, propertyInfo, field);
        }
    
        context.parent().generateAdapterIfNecessary(propertyInfo, field);
    
        QName st = propertyInfo.getSchemaType();
        if (st != null) field.annotate2(XmlSchemaTypeWriter.class).name(st.getLocalPart()).namespace(st.getNamespaceURI());
    
        if (propertyInfo.inlineBinaryData()) field.annotate(XmlInlineBinaryData.class);
    }

    protected final void annotateReference(ClassOutlineImpl context, CPropertyInfo propertyInfo, JAnnotatable field) {
        CReferencePropertyInfo rp = (CReferencePropertyInfo) propertyInfo;
    
        TODO.prototype();
        // this is just a quick hack to get the basic test working
    
        Collection<CElement> elements = rp.getElements();
    
        XmlElementRefWriter refw;
        if (elements.size() == 1) {
            refw = field.annotate2(XmlElementRefWriter.class);
            CElement e = elements.iterator().next();
            refw.name(e.getElementName().getLocalPart()).namespace(e.getElementName().getNamespaceURI()).type(e.getType().toType(context.parent(), IMPLEMENTATION));
            if (context.parent().getModel().options.target.isLaterThan(SpecVersion.V2_2)) refw.required(rp.isRequired());
        } else if (elements.size() > 1) {
            XmlElementRefsWriter refsw = field.annotate2(XmlElementRefsWriter.class);
            for (CElement e : elements) {
                refw = refsw.value();
                refw.name(e.getElementName().getLocalPart()).namespace(e.getElementName().getNamespaceURI()).type(e.getType().toType(context.parent(), IMPLEMENTATION));
                if (context.parent().getModel().options.target.isLaterThan(SpecVersion.V2_2)) refw.required(rp.isRequired());
            }
        }
    
        if (rp.isMixed()) field.annotate(XmlMixed.class);
    
        NClass dh = rp.getDOMHandler();
        if (dh != null) {
            XmlAnyElementWriter xaew = field.annotate2(XmlAnyElementWriter.class);
            xaew.lax(rp.getWildcard().allowTypedObject);
    
            final JClass value = dh.toType(context.parent(), IMPLEMENTATION);
            if (!value.equals(context.parent().getCodeModel().ref(W3CDomHandler.class))) {
                xaew.value(value);
            }
        }
    
    }

    /**
     * Annotate the attribute property 'field'
     */
    protected final void annotateAttribute(ClassOutlineImpl context, CPropertyInfo propertyInfo, JAnnotatable field) {
        CAttributePropertyInfo ap = (CAttributePropertyInfo) propertyInfo;
        QName attName = ap.getXmlName();
    
        // [RESULT]
        // @XmlAttribute(name="foo", required=true, namespace="bar://baz")
        XmlAttributeWriter xaw = field.annotate2(XmlAttributeWriter.class);
    
        final String generatedName = attName.getLocalPart();
        final String generatedNS = attName.getNamespaceURI();
    
        // Issue 570; always force generating name="" when do it when
        // globalBindings underscoreBinding is set to non default value
        // generate name property?
        if (!generatedName.equals(ap.getName(false)) || !generatedName.equals(ap.getName(true)) || (context.parent().getModel().getNameConverter() != NameConverter.standard)) {
            xaw.name(generatedName);
        }
    
        // generate namespace property?
        if (!generatedNS.equals("")) { // assume attributeFormDefault ==
                                       // unqualified
            xaw.namespace(generatedNS);
        }
    
        // generate required property?
        if (ap.isRequired()) {
            xaw.required(true);
        }
    }

    /**
     * Annotate the element property 'field'
     */
    protected final void annotateElement(ClassOutlineImpl context, CPropertyInfo propertyInfo, JAnnotatable field, JType exposedType) {
        CElementPropertyInfo ep = (CElementPropertyInfo) propertyInfo;
        List<CTypeRef> types = ep.getTypes();
    
        if (ep.isValueList()) {
            field.annotate(XmlList.class);
        }
    
        if (types.size() == 1) {
            CTypeRef t = types.get(0);
            writeXmlElementAnnotation(context, propertyInfo, field, t, resolve(context, t, IMPLEMENTATION), false, exposedType);
        } else {
            for (CTypeRef t : types) {
                // generate @XmlElements
                writeXmlElementAnnotation(context, propertyInfo, field, t, resolve(context, t, IMPLEMENTATION), true, exposedType);
            }
            xesw = null;
        }
    }

    /**
     * Generate the simplest XmlElement annotation possible taking all semantic
     * optimizations into account. This method is essentially equivalent to:
     *
     * xew.name(ctype.getTagName().getLocalPart())
     * .namespace(ctype.getTagName().getNamespaceURI()) .type(jtype)
     * .defaultValue(ctype.getDefaultValue());
     *
     * @param field
     * @param ctype
     * @param jtype
     * @param checkWrapper true if the method might need to generate XmlElements
     */
    protected final void writeXmlElementAnnotation(ClassOutlineImpl context, CPropertyInfo propertyInfo, JAnnotatable field, CTypeRef ctype, JType jtype, boolean checkWrapper, JType exposedType) {
    
        // lazily create - we don't know if we need to generate anything yet
        XmlElementWriter xew = null;
    
        // these values are used to determine how to optimize the generated
        // annotation
        XmlNsForm formDefault = context._package().getElementFormDefault();
        String propName = propertyInfo.getName(false);
    
        String enclosingTypeNS;
    
        if (context.target.getTypeName() == null)
            enclosingTypeNS = context._package().getMostUsedNamespaceURI();
        else
            enclosingTypeNS = context.target.getTypeName().getNamespaceURI();
    
        // generate the name property?
        String generatedName = ctype.getTagName().getLocalPart();
        if (!generatedName.equals(propName)) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.name(generatedName);
        }
    
        // generate the namespace property?
        String generatedNS = ctype.getTagName().getNamespaceURI();
        if (((formDefault == XmlNsForm.QUALIFIED) && !generatedNS.equals(enclosingTypeNS)) || ((formDefault == XmlNsForm.UNQUALIFIED) && !generatedNS.equals(""))) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.namespace(generatedNS);
        }
    
        // generate the required() property?
        CElementPropertyInfo ep = (CElementPropertyInfo) propertyInfo;
        if (ep.isRequired() && exposedType.isReference()) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.required(true);
        }
    
        // generate the type property?
    
        // I'm not too sure if this is the right place to handle this, but
        // if the schema definition is requiring this element, we should point
        // to a primitive type,
        // not wrapper type (to correctly carry forward the required semantics.)
        // if it's a collection, we can't use a primitive, however.
        if (ep.isRequired() && !propertyInfo.isCollection()) jtype = jtype.unboxify();
    
        // when generating code for 1.4, the runtime can't infer that
        // ArrayList<Foo> derives
        // from Collection<Foo> (because List isn't parameterized), so always
        // expclitly
        // generate @XmlElement(type=...)
        if (!jtype.equals(exposedType) || (context.parent().getModel().options.runtime14 && propertyInfo.isCollection())) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.type(jtype);
        }
    
        // generate defaultValue property?
        final String defaultValue = ctype.getDefaultValue();
        if (defaultValue != null) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.defaultValue(defaultValue);
        }
    
        // generate the nillable property?
        if (ctype.isNillable()) {
            if (xew == null) xew = getXew(checkWrapper, field);
            xew.nillable(true);
        }
    }

    /**
     * return the Java type for the given type reference in the model.
     */
    protected final JType resolve(ClassOutlineImpl context, CTypeRef typeRef, Aspect a) {
        return context.parent().resolve(typeRef, a);
    }

    /**
     * Compute the type of a {@link CPropertyInfo}
     * 
     * @param aspect
     */
    protected final JType getType(ClassOutlineImpl context, CPropertyInfo propertyInfo, final Aspect aspect) {
        if (propertyInfo.getAdapter() != null) return propertyInfo.getAdapter().customType.toType(context.parent(), aspect);
    
        JType t;
        if (propertyInfo.baseType != null) {
            t = propertyInfo.baseType;
        } else {
            final Set<JType> types = new HashSet<>();
            for (final CTypeInfo typeInfo : propertyInfo.ref()) {
                types.add(typeInfo.toType(context.parent(), aspect));
                if (typeInfo instanceof CElementInfo) {
                    for (final CElementInfo e : ((CElementInfo) typeInfo).getSubstitutionMembers()) {
                        types.add(e.toType(context.parent(), aspect));
                    }
                }
            }
    
            t = TypeUtil.getCommonBaseType(context.parent().getCodeModel(), types);
        }
    
        // if item type is unboxable, convert t=Integer -> t=int
        // the in-memory data structure can't have primitives directly,
        // but this guarantees that items cannot legal hold null,
        // which helps us improve the boundary signature between our
        // data structure and user code
        if (propertyInfo.isUnboxable()) t = t.unboxify();
        return t;
    }

    /**
     * Returns contents to be added to javadoc.
     */
    protected final List<Object> listPossibleTypes(Outline context, CPropertyInfo propertyInfo) {
        List<Object> r = new ArrayList<Object>();
        for (CTypeInfo tt : propertyInfo.ref()) {
            JType t = tt.getType().toType(context, Aspect.EXPOSED);
            if (t.isPrimitive() || t.isArray())
                r.add(t.fullName());
            else {
                r.add(t);
                r.add("\n");
            }
        }
    
        return r;
    }

    public final @NotNull Configuration configuration() {
        return this.configuration;
    }

}