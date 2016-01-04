package fgd.tools.extensions.xjc;

import static java.util.Objects.*;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.tools.internal.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.internal.xjc.generator.bean.field.FieldRendererFactory;

public class ModernizationFieldRendererFactory extends FieldRendererFactory {

    private final @NotNull Configuration configuration;

    public ModernizationFieldRendererFactory(@NotNull final Configuration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    @Override
    public FieldRenderer getList(JClass coreList) {
        return new MultiValuedFieldRenderer(this.configuration);
    }

    @Override
    public FieldRenderer getContentList(JClass coreList) {
        return new MultiValuedFieldRenderer(this.configuration);
    }

    @Override
    public FieldRenderer getSingle() {
    	return new SingleValuedFieldRenderer(this.configuration);
    }

}
