package fgd.tools.extensions.xjc;

import static java.util.Objects.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldRef;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JPrimitiveType;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.outline.FieldAccessor;
import com.sun.tools.internal.xjc.outline.FieldOutline;

abstract class AbstractFieldAccessor implements FieldAccessor {

	@NotNull
	private final FieldOutline owner;

	/**
	 * Evaluates to the target object this accessor should access.
	 */
	@NotNull
	private final JExpression target;

	@NotNull
	private final JFieldVar field;

	/**
	 * Reference to the field bound by the target object.
	 */
	@NotNull
	private final JFieldRef fieldReference;

	protected AbstractFieldAccessor(@NotNull FieldOutline owner, @NotNull JExpression target, @NotNull JFieldVar field) {
		this.owner = requireNonNull(owner);
		this.target = requireNonNull(target);
		this.field = requireNonNull(field);
		this.fieldReference = requireNonNull(target.ref(field));
	}

	@NotNull
	public final JExpression target() {
		return this.target;
	}

	@NotNull
	public final JFieldVar field() {
		return this.field;
	}

	@NotNull
	public final JFieldRef fieldReference() {
		return this.fieldReference;
	}

	/**
	 * Gets the {@link FieldOutline} from which
	 * this object is created.
	 */
	@Override
	public final FieldOutline owner() {
		return this.owner;
	}

	/**
	 * Short for <tt>owner().getPropertyInfo()</tt>
	 */
	@Override
	public final CPropertyInfo getPropertyInfo() {
		return owner().getPropertyInfo();
	}

	/**
	 * Gets the name of the getter method.
	 *
	 * <p>
	 * This encapsulation is necessary because sometimes we use {@code isXXXX}
	 * as the method name.
	 */
	protected String getterName() {
		final String name = getPropertyInfo().getName(true);
		final JType type =  field().type();
		final JPrimitiveType primitiveType = type.boxify().getPrimitiveType();
		final String simpleName = null != primitiveType ? primitiveType.fullName() : "";
		if (owner().parent().parent().getModel().options.enableIntrospection) {
			return ((type.isPrimitive() && "boolean".equals(simpleName)) ? "is" : "get") + name;
		} else {
			return ("boolean".equals(simpleName) ? "is" : "get") + name;
		}
	}

	protected String setterName() {
		return "set" + getPropertyInfo().getName(true);
	}

}