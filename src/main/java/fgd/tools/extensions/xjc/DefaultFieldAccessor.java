package fgd.tools.extensions.xjc;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.outline.FieldOutline;

class DefaultFieldAccessor extends AbstractFieldAccessor {

	public DefaultFieldAccessor(@NotNull FieldOutline owner, @NotNull JExpression target, @NotNull JFieldVar field) {
		super(owner, target, field);
	}

	/**
     * Dumps everything in this field into the given variable.
     * 
     * <p>
     * This generates code that accesses the field from outside.
     * 
     * @param block
     *      The code will be generated into this block.
     * @param var
     *      Variable whose type is {@link FieldOutline#getRawType()}
     */
	@Override
	public final void toRawValue(JBlock block, JVar var) {
		block.assign(var, target().invoke(getterName()));
	}

    /**
     * Sets the value of the field from the specified expression.
     * 
     * <p>
     * This generates code that accesses the field from outside.
     * 
     * @param block
     *      The code will be generated into this block.
     * @param uniqueName
     *      Identifier that the caller guarantees to be unique in
     *      the given block. When the callee needs to produce additional
     *      variables, it can do so by adding suffixes to this unique
     *      name. For example, if the uniqueName is "abc", then the 
     *      caller guarantees that any identifier "abc.*" is unused
     *      in this block.
     * @param var
     *      The expression that evaluates to a value of the type
     *      {@link FieldOutline#getRawType()}.
     */
	@Override
	public final void fromRawValue(JBlock block, String uniqueName, JExpression var) {
		block.invoke(target(), setterName()).arg(var);
	}

    /**
     * Generates a code fragment to remove any "set" value
     * and move this field to the "unset" state.
     * 
     * @param body
     *      The code will be appended at the end of this block.
     */
	@Override
	public void unsetValues(JBlock body) {
		body.assign(fieldReference(), JExpr._null());
	}

    /**
     * Return an expression that evaluates to true only when
     * this field has a set value(s).
     * 
     * @return null
     *      if the isSetXXX/unsetXXX method does not make sense 
     *      for the given field.
     */
	@Override
	public JExpression hasSetValue() {
		return fieldReference().ne(JExpr._null());
	}
}