package fgd.tools.extensions.xjc;

import static java.util.Objects.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.outline.ClassOutline;
import com.sun.tools.internal.xjc.outline.FieldAccessor;
import com.sun.tools.internal.xjc.outline.FieldOutline;

final class ModernizationFieldOutline implements FieldOutline {
    @NotNull private final ClassOutlineImpl context;
    @NotNull private final CPropertyInfo propertyInfo;
    @NotNull private final JType exposedType;
    @NotNull private final JFieldVar field;

    public ModernizationFieldOutline(ClassOutlineImpl context, CPropertyInfo propertyInfo, JFieldVar field, JType exposedType) {
        this.context = requireNonNull(context);
        this.propertyInfo = requireNonNull(propertyInfo);
        this.exposedType = requireNonNull(exposedType);
        this.field = requireNonNull(field);
	}

	/**
     * Gets the enclosing {@link ClassOutline}.
     */
	@Override
    public ClassOutline parent() {
		return this.context;
	}

    /** Gets the corresponding model object. */
    @Override
    public CPropertyInfo getPropertyInfo() {
		return this.propertyInfo;
	}

    /**
     * Gets the type of the "raw value".
     * 
     * <p>
     * This type can represent the entire value of this field.
     * For fields that can carry multiple values, this is an array.
     *
     * <p>
     * This type allows the client of the outline to generate code
     * to set/get values from a property.
     */
    @Override
    public JType getRawType() {
		return this.exposedType;
	}
    
    /**
     * Creates a new {@link FieldAccessor} of this field
     * for the specified object.
     * 
     * @param targetObject
     *      Evaluates to an object, and the field on this object
     *      will be accessed.
     */
    @Override
	public FieldAccessor create(JExpression targetObject) {
    	return new DefaultFieldAccessor(this, requireNonNull(targetObject), this.field);
	}
	
}