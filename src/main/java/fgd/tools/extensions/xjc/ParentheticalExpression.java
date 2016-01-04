package fgd.tools.extensions.xjc;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JExpressionImpl;
import com.sun.codemodel.internal.JFormatter;

final class ParentheticalExpression extends JExpressionImpl {
    @NotNull final private JExpression e;

    ParentheticalExpression(@NotNull final JExpression e) {
        this.e = e;
    }

    @Override
    public void generate(final JFormatter f) {
        f.p('(').g(this.e).p(')');
    }
}