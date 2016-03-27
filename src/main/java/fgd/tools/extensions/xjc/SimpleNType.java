package fgd.tools.extensions.xjc;

import static java.util.Objects.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.model.nav.NType;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.Outline;

final class SimpleNType implements NType {
	@NotNull
    private final String fullName;

    public SimpleNType(@NotNull final String fullName) {
        this.fullName = requireNonNull(fullName, "fullName cannot be null");
    }

    @Override
    public String fullName() {
        return this.fullName;
    }

    @Override
    public boolean isBoxedType() {
        return false;
    }

    @Override
    public JType toType(final Outline o, final Aspect a) {
        return new JDirectClassEx(requireNonNull(o).getCodeModel(), this.fullName);
    }

}
