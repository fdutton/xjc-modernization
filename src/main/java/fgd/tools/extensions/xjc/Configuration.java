package fgd.tools.extensions.xjc;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;

public interface Configuration {

    boolean android();

    boolean finalizeMethods();

    boolean findbugs();

    boolean jetbrains();

    boolean jsr303();

    boolean jsr305();

    boolean lombok();

    boolean privatizeFields();

    boolean useOptional();

    @NotNull Iterable<JClass> notNullableAnnotations(final @NotNull JCodeModel model);

    @NotNull Iterable<JClass> nullableAnnotations(final @NotNull JCodeModel model);

}