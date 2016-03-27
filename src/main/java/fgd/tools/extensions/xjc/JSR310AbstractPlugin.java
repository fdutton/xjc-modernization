package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;

import static java.util.Objects.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.internal.JAnnotationArrayMember;
import com.sun.codemodel.internal.JAnnotationUse;
import com.sun.codemodel.internal.JAnnotationValue;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JOp;
import com.sun.codemodel.internal.JPackage;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.Plugin;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.tools.internal.xjc.outline.PackageOutline;
import com.sun.tools.internal.xjc.reader.xmlschema.SimpleTypeBuilder;

public abstract class JSR310AbstractPlugin extends Plugin {

    private final @NotNull Set</*@NotNull*/ NTypeUse> replaced = new HashSet<>();

    protected void replace(final @NotNull String schemaType, final @NotNull NTypeUse typeUse) {
        // TODO: Limit schemaType to time-based simple types.
        requireNonNull(schemaType, "schemaType cannot be null");
        requireNonNull(typeUse, "typeUse cannot be null");
        SimpleTypeBuilder.builtinConversions.put(schemaType, typeUse);
        replaced.add(typeUse);
    }

    @Override
    public boolean run(Outline model, Options opts, ErrorHandler errHandler) throws SAXException {
        for (final NTypeUse typeUse : this.replaced) {
            assert null != typeUse;
            emitAdapter(model, opts, errHandler, typeUse);
        }

        return true;
    }

    private void emitAdapter(Outline model, Options opts, ErrorHandler errHandler, final @NotNull NTypeUse typeUse) throws SAXException {
        final JType type = typeUse.toType(model, Aspect.IMPLEMENTATION);
        final String adapterName = type.name() + "Adapter";

        for (final PackageOutline po : model.getAllPackageContexts()) {
            final JPackage p = po._package();

            JClass adapterClass = p._getClass(adapterName);
            if (null == adapterClass) {
                try {
                    adapterClass = emitAdapter(p, type, adapterName);
                } catch (JClassAlreadyExistsException e) {
                    errHandler.warning(new SAXParseException(e.getMessage(), null));
                }
            }

            getXmlJavaTypeAdaptersValue(p)
                .annotate(XmlJavaTypeAdapter.class)
                .param("type", type)
                .param("value", adapterClass)
                ;
        }
    }

    private static JAnnotationArrayMember getXmlJavaTypeAdaptersValue(final JPackage p) {
        requireNonNull(p, "p cannot be null");

        final JAnnotationUse xmlJavaTypeAdapters = getXmlJavaTypeAdapters(p);

        JAnnotationArrayMember a;
        try {
            final Map<String, JAnnotationValue> annotationMembers = xmlJavaTypeAdapters.getAnnotationMembers();
            a = (JAnnotationArrayMember) annotationMembers.get("value");
            if (null == a) {
                a = xmlJavaTypeAdapters.paramArray("value");
            }
        } catch (NullPointerException e) {
            a = xmlJavaTypeAdapters.paramArray("value");
        }
        return a;
    }

    private static JAnnotationUse getXmlJavaTypeAdapters(final JPackage p) {
        final Collection<JAnnotationUse> annotations = p.annotations();
        for (final JAnnotationUse annotationUse : annotations) {
            if (XmlJavaTypeAdapters.class.getName().equals(annotationUse.getAnnotationClass().fullName())) {
                return annotationUse;
            }
        }
        return p.annotate(XmlJavaTypeAdapters.class);
    }

    private static JDefinedClass emitAdapter(final JPackage p, final JType boundType, final String name) throws JClassAlreadyExistsException {
        final JDefinedClass c = p._class(FINAL, name);
        c._extends(XmlAdapter.class);
        c._extends(c._extends().narrow(String.class).narrow(boundType));

        final JMethod u = c.method(PUBLIC, boundType, "unmarshal");
        final JVar s = u.param(FINAL, String.class, "v");
        u.annotate(Override.class);
        u.body()._return(JOp.cond(
            JExpr._null().eq(s),
            JExpr._null(),
            boundType.boxify().staticInvoke("parse").arg(s)
        ));

        final JMethod m = c.method(PUBLIC, String.class, "marshal");
        final JVar d = m.param(FINAL, boundType, "v");
        m.annotate(Override.class);
        m.body()._return(JOp.cond(
            JExpr._null().eq(d),
            JExpr._null(),
            d.invoke("toString")
        ));

        return c;
    }

}
