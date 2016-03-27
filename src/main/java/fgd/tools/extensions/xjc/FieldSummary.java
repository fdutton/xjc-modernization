package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;
import static fgd.tools.extensions.xjc.XJCUtils.*;

import static java.util.Objects.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JPrimitiveType;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.outline.FieldOutline;

final class FieldSummary {
	@NotNull public final FieldOutline def;
	@NotNull public final JFieldVar var;
	@NotNull public final String name;
	@NotNull public final JType type;
	@NotNull public final String getterName;
	@NotNull public final String setterName;
	public final boolean isRequired;
	public final boolean isStaticField;

	public FieldSummary(@NotNull final FieldOutline def, final boolean enableIntrospection) {
		this.def = def;
		this.name = def.getPropertyInfo().getName(false);
		this.var = requireNonNull(def.parent().implClass.fields().get(this.name));
		this.isRequired = isRequired(def);
		this.isStaticField = (this.var.mods().getValue() & STATIC) != 0;
        this.type = var.type();
		final JPrimitiveType primitiveType = this.type.boxify().getPrimitiveType();
		final String simpleName = null != primitiveType ? primitiveType.fullName() : "";
		this.getterName = (((!enableIntrospection || this.type.isPrimitive()) && "boolean".equals(simpleName)) ? "is" : "get") + def.getPropertyInfo().getName(true);
		this.setterName = "set" + def.getPropertyInfo().getName(true);
	}
}