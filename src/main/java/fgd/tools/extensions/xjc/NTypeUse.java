package fgd.tools.extensions.xjc;

import static javax.xml.XMLConstants.*;
import static com.sun.tools.internal.xjc.model.CCustomizations.*;
import static com.sun.xml.internal.bind.v2.model.core.ID.*;

import javax.activation.MimeType;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.QName;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.model.CAdapter;
import com.sun.tools.internal.xjc.model.CCustomizations;
import com.sun.tools.internal.xjc.model.CNonElement;
import com.sun.tools.internal.xjc.model.nav.NType;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.xml.internal.bind.v2.model.annotation.Locatable;
import com.sun.xml.internal.bind.v2.model.core.ID;
import com.sun.xml.internal.bind.v2.runtime.Location;
import com.sun.xml.internal.xsom.XSComponent;

abstract class NTypeUse implements CNonElement, Location {

    private static final Locator EMPTY_LOCATOR = new LocatorImpl() {{
        setColumnNumber(-1);
        setLineNumber(-1);
    }};

    private final QName q;
    private final NType n;

    public NTypeUse(@NotNull final String c) {
        this(new SimpleNType(c), "\u0000");
    }

    public NTypeUse(@NotNull final String c, @NotNull final String t) {
        this(new SimpleNType(c), t);
    }

    public NTypeUse(@NotNull final NType n, @NotNull final String t) {
        this.q = new QName(W3C_XML_SCHEMA_NS_URI, t);
        this.n = n;
    }

//    @Override
//    public JExpression createConstant(final Outline o, final XmlString s) {
//        return null;
//    }

    @Override
    public CAdapter getAdapterUse() {
        return null;
    }

    @Override
    public MimeType getExpectedMimeType() {
        return null;
    }

    @Override
    public CNonElement getInfo() {
        return this;
    }

    @Override
    public ID idUse() {
        return NONE;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public QName getTypeName() {
        return this.q;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public boolean canBeReferencedByIDREF() {
        return false;
    }

    @Override
    public NType getType() {
        return this.n;
    }

    @Override
    public Location getLocation() {
        return this;
    }

    @Override
    public Locatable getUpstream() {
        return null;
    }

    @Override
    public JType toType(Outline o, Aspect a) {
        return getType().toType(o, a);
    }

    @Override
    public CCustomizations getCustomizations() {
        return EMPTY;
    }

    @Override
    public Locator getLocator() {
        return EMPTY_LOCATOR;
    }

    @Override
    public XSComponent getSchemaComponent() {
        return null;
    }

}