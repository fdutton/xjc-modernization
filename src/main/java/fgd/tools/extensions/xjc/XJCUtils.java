package fgd.tools.extensions.xjc;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JExpression;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.outline.FieldOutline;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.internal.xsom.XSAnnotation;
import com.sun.xml.internal.xsom.XSAttributeUse;
import com.sun.xml.internal.xsom.XSComponent;
import com.sun.xml.internal.xsom.XSParticle;

import fgd.internal.org.apache.commons.lang3.text.WordUtils;

public final class XJCUtils {

    @NotNull
    public static ParentheticalExpression parenthesis(@NotNull final JExpression e) {
        return new ParentheticalExpression(e);
    }

    @NotNull
    public static JBlock clearBody(final JBlock body) {
        Objects.requireNonNull(body);

        try {
            final Field field = body.getClass().getDeclaredField("content");
            field.setAccessible(true);
            List.class.cast(field.get(body)).clear();
            body.pos(0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WrappedReflectiveOperationException("Unable to access content field of JBlock", e);
        }

        return body;
    }

    public static boolean isOptional(@NotNull final FieldOutline f) {
        return !isRequired(f);
    }

    public static boolean isRequired(@NotNull final FieldOutline f) {
        Objects.requireNonNull(f);
        return isRequired(f.getPropertyInfo());
    }

	public static boolean isRequired(final CPropertyInfo propertyInfo) {
        Objects.requireNonNull(propertyInfo);
		switch (propertyInfo.kind()) {
            case ATTRIBUTE: return ((XSAttributeUse) propertyInfo.getSchemaComponent()).isRequired();
            case ELEMENT: return !propertyInfo.isCollection() && !BigInteger.ZERO.equals(((XSParticle) propertyInfo.getSchemaComponent()).getMinOccurs());
            default: return false;
        }
	}

	public static String extractDocumentation(@NotNull final CPropertyInfo propertyInfo) {
		Objects.requireNonNull(propertyInfo);

		final StringBuilder sb = new StringBuilder();

        final XSComponent schemaComponent = propertyInfo.getSchemaComponent();
        if (null != schemaComponent) {
            if (schemaComponent instanceof XSAttributeUse) {
        		XSAttributeUse attributeUse = (XSAttributeUse) schemaComponent;
        		XSAnnotation ann = attributeUse.getDecl().getAnnotation(false);
                if (null != ann) {
                	BindInfo a = (BindInfo) ann.getAnnotation();
                	String s = a.getDocumentation().replaceAll("\\s+", " ").trim();
                	sb.append(WordUtils.wrap(s, 60, "\n", false));
                }
            } else if (schemaComponent instanceof XSParticle) {
            	XSParticle particle = (XSParticle) schemaComponent;
        		XSAnnotation ann = particle.getTerm().getAnnotation(false);
                if (null != ann) {
                	BindInfo a = (BindInfo) ann.getAnnotation();
                	String s = a.getDocumentation().replaceAll("\\s+", " ").trim();
                	sb.append(WordUtils.wrap(s, 60, "\n", false));
                }
            } else {
            	System.out.println(propertyInfo.parent().getType().fullName() + "." + propertyInfo.getName(false) + ":" + schemaComponent.getClass().getName());
            }
        }

        return sb.toString();
	}

    private XJCUtils() {
        super();
    }

}

final class WrappedReflectiveOperationException extends RuntimeException {
    private static final long serialVersionUID = -1;

    /**
     * Constructs an instance of this class.
     *
     * @param   message
     *          the detail message, can be null
     * @param   cause
     *          the {@code ReflectiveOperationException}
     *
     * @throws  NullPointerException
     *          if the cause is {@code null}
     */
    public WrappedReflectiveOperationException(String message, ReflectiveOperationException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    /**
     * Constructs an instance of this class.
     *
     * @param   cause
     *          the {@code ReflectiveOperationException}
     *
     * @throws  NullPointerException
     *          if the cause is {@code null}
     */
    public WrappedReflectiveOperationException(ReflectiveOperationException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return  the {@code ReflectiveOperationException} which is the cause of this exception.
     */
    @Override
    public ReflectiveOperationException getCause() {
        return (ReflectiveOperationException) super.getCause();
    }

    /**
     * Called to read the object from a stream.
     *
     * @throws  InvalidObjectException
     *          if the object is invalid or has a cause that is not
     *          an {@code ReflectiveOperationException}
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof ReflectiveOperationException))
            throw new InvalidObjectException("Cause must be an ReflectiveOperationException");
    }
}
