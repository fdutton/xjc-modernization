package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;

import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.internal.JAnnotationArrayMember;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JOp;
import com.sun.codemodel.internal.JPackage;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.BadCommandLineException;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.Plugin;
import com.sun.tools.internal.xjc.model.TypeUse;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.tools.internal.xjc.outline.PackageOutline;
import com.sun.tools.internal.xjc.reader.xmlschema.SimpleTypeBuilder;

public abstract class JSR310AbstractPlugin extends Plugin {

    private final NTypeUse durationType;
    private final NTypeUse instantType;
    private final NTypeUse periodType;

    protected JSR310AbstractPlugin(@NotNull final NTypeUse durationType, @NotNull final NTypeUse instantType, @NotNull final NTypeUse periodType) {
		this.durationType = Objects.requireNonNull(durationType, "durationType cannot be null");
		this.instantType = Objects.requireNonNull(instantType, "instantType cannot be null");
		this.periodType = Objects.requireNonNull(periodType, "periodType cannot be null");
	}

    @Override
    public void onActivated(Options opts) throws BadCommandLineException {
        Map<String, TypeUse> m = SimpleTypeBuilder.builtinConversions;
        m.put("dateTime",       instantType);
        m.put("date",           instantType);
        m.put("time",           instantType);
        m.put("gYearMonth",     instantType);
        m.put("gYear",          instantType);
        m.put("gMonthDay",      instantType);
        m.put("gDay",           instantType);
        m.put("gMonth",         instantType);
        m.put("duration",       periodType);
    }

    @Override
    public boolean run(Outline model, Options opts, ErrorHandler errHandler) throws SAXException {
        final JType duration = durationType.toType(model, Aspect.IMPLEMENTATION);
        final JType instant = instantType.toType(model, Aspect.IMPLEMENTATION);
        final JType period = periodType.toType(model, Aspect.IMPLEMENTATION);

        for (final PackageOutline po : model.getAllPackageContexts()) {
            final JPackage p = po._package();

            JClass durationAdapter = p._getClass("DurationAdapter");
            if (null == durationAdapter) {
                try {
                    durationAdapter = emitAdapter(p, duration, "DurationAdapter");
                } catch (JClassAlreadyExistsException e) {
                    errHandler.warning(new SAXParseException(e.getMessage(), null));
                }
            }
            JClass instantAdapter = p._getClass("InstantAdapter");
            if (null == instantAdapter) {
                try {
                    instantAdapter = emitAdapter(p, instant, "InstantAdapter");
                } catch (JClassAlreadyExistsException e) {
                    errHandler.warning(new SAXParseException(e.getMessage(), null));
                }
            }
            JClass periodAdapter = p._getClass("PeriodAdapter");
            if (null == periodAdapter) {
                try {
                    periodAdapter = emitAdapter(p, period, "PeriodAdapter");
                } catch (JClassAlreadyExistsException e) {
                    errHandler.warning(new SAXParseException(e.getMessage(), null));
                }
            }

            final JAnnotationArrayMember a = p.annotate(XmlJavaTypeAdapters.class).paramArray("value");
            a.annotate(XmlJavaTypeAdapter.class).param("type", duration).param("value", durationAdapter);
            a.annotate(XmlJavaTypeAdapter.class).param("type", instant).param("value", instantAdapter);
            a.annotate(XmlJavaTypeAdapter.class).param("type", period).param("value", periodAdapter);
        }

        return true;
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
