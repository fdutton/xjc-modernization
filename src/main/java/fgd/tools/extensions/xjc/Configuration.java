package fgd.tools.extensions.xjc;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;

public interface Configuration {

    boolean android();

    boolean finalizeFields();

    boolean finalizeMethods();

    boolean findbugs();

    boolean jetbrains();

    boolean jsr303();

    boolean jsr305();

    boolean jsr349();

    boolean lombok();

    boolean privatizeFields();

    boolean useOptional();

    @NotNull Iterable<JClass> notNullableAnnotations(@NotNull JCodeModel codeModel);

    @NotNull Iterable<JClass> nullableAnnotations(@NotNull JCodeModel codeModel);

}