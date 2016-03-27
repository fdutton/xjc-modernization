package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JExpr.*;
import static com.sun.codemodel.internal.JMod.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JTypeVar;
import com.sun.codemodel.internal.JVar;
import com.sun.tools.internal.xjc.BadCommandLineException;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.Plugin;
import com.sun.tools.internal.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.internal.xjc.generator.bean.field.SingleField;
import com.sun.tools.internal.xjc.outline.ClassOutline;
import com.sun.tools.internal.xjc.outline.FieldOutline;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.tools.internal.xjc.outline.PackageOutline;

public final class ModernizationPlugin extends Plugin implements Configuration {

    private boolean finalizeFields = false;
    private boolean finalizeMethods = false;
    private boolean privatizeFields = false;
    private boolean useOptional = false;
    private boolean jsr303 = false;
    private boolean jsr305 = false;
    private boolean jsr349 = false;
    private boolean findbugs = false;
    private boolean jetbrains = false;
    private boolean lombok = false;
    private boolean android = false;

    @Override
    public final boolean android() {
        return this.android;
    }

    @Override
    public final boolean finalizeFields() {
        return this.finalizeFields;
    }

    @Override
    public final boolean finalizeMethods() {
        return this.finalizeMethods;
    }

    @Override
    public boolean findbugs() {
        return this.findbugs;
    }

    @Override
    public boolean jetbrains() {
        return this.jetbrains;
    }

    @Override
    public boolean jsr303() {
        return this.jsr303;
    }

    @Override
    public boolean jsr305() {
        return this.jsr305;
    }
    
    @Override
    public boolean jsr349() {
        return this.jsr349;
    }

    @Override
    public boolean lombok() {
        return this.lombok;
    }

    @Override
    public final boolean privatizeFields() {
        return this.privatizeFields;
    }

    @Override
    public final boolean useOptional() {
        return this.useOptional;
    }

    /**
     * Gets the command line parameter used to enable this plug-in.
     *
     * <p/>
     * For example, if this method returns "modernize" then having
     * "-modernize" on the command line instructs XJC to enable
     * this plug-in. A plug-in must be explicitly enabled before
     * XJC will invoke any other methods of {@link Plugin}.
     *
     * <p/>
     * Starting with XJC version 2.1, when XJC enables a plug-in, it
     * will then invoke {@link #parseArgument(Options, String[], int)}
     * to allow the plug-in to handle additional arguments.
     */
    @Override
    public String getOptionName() {
        return "modernize";
    }

    /**
     * Gets the description of this plug-on. Used to generate
     * a usage screen.
     *
     * @return
     *      localized description message. should be terminated by \n.
     */
    @Override
    public String getUsage() {
        return ""
            + "  -modernize         :  add Java 8 support (see the following sub-arguments)\n"
            + "    -finalize-fields  mark all required fields as final\n"
            + "    -finalize-methods mark all implementation methods as final\n"
            + "    -privatize-fields mark all implementation fields as private\n"
            + "    -j8-optional      return Optional from nullable getters\n"
            + "    -JSR303           add nullability annotations from Bean Validation API\n"
            + "    -JSR305           add nullability annotations from Annotations for Software Defect Detection\n"
            + "    -findbugs         add nullability annotations from Findbugs\n"
            + "    -lombok           add nullability annotations from Project Lombok\n"
            + "    -android          add nullability annotations from Android's support-annotations package"
            ;
    }

    /**
     * Parses an option <code>args[i]</code> and augment
     * the <code>opt</code> object appropriately, then return
     * the number of tokens consumed.
     *
     * <p>
     * The callee doesn't need to recognize the option that the
     * getOptionName method returns.
     *
     * <p>
     * Once a plugin is activated, this method is called
     * for options that XJC didn't recognize. This allows
     * a plugin to define additional options to customize
     * its behavior.
     *
     * <p>
     * Since options can appear in no particular order,
     * XJC allows sub-options of a plugin to show up before
     * the option that activates a plugin (one that's returned
     * by {@link #getOptionName().)
     *
     * But nevertheless a {@link Plugin} needs to be activated
     * to participate in further processing.
     *
     * @return
     *      0 if the argument is not understood.
     *      Otherwise return the number of tokens that are
     *      consumed, including the option itself.
     *      (so if you have an option like "-foo 3", return 2.)
     * @exception BadCommandLineException
     *      If the option was recognized but there's an error.
     *      This halts the argument parsing process and causes
     *      XJC to abort, reporting an error.
     */
    @Override
    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        if ("-finalize-fields".equals(args[i])) {
            this.finalizeFields = true;
            return 1;
        }
        if ("-finalize-methods".equals(args[i])) {
            this.finalizeMethods = true;
            return 1;
        }
        if ("-privatize-fields".equals(args[i])) {
            this.privatizeFields = true;
            return 1;
        }
        if ("-j8-optional".equals(args[i])) {
            this.useOptional = true;
            return 1;
        }
        if ("-JSR303".equals(args[i])) {
            this.jsr303 = true;
            return 1;
        }
        if ("-JSR305".equals(args[i])) {
            this.jsr305 = true;
            return 1;
        }
        if ("-JSR349".equals(args[i])) {
            this.jsr303 = true;
            this.jsr349 = true;
            return 1;
        }
        if ("-findbugs".equals(args[i])) {
            this.findbugs = true;
            return 1;
        }
        if ("-jetbrains".equals(args[i])) {
            this.jetbrains = true;
            return 1;
        }
        if ("-lombok".equals(args[i])) {
            this.lombok = true;
            return 1;
        }
        if ("-android".equals(args[i])) {
            this.android = true;
            return 1;
        }

        return 0;
    }

    /**
     * Notifies a plugin that it's activated.
     *
     * <p>
     * This method is called when a plugin is activated
     * through the command line option (as specified by {@link #getOptionName()}.
     *
     * <p>
     * This is a good opportunity to use
     * {@link Options#setFieldRendererFactory(FieldRendererFactory, Plugin)}
     * if a plugin so desires.
     *
     * <p>
     * Noop by default.
     *
     * @since JAXB 2.0 EA4
     */
    public void onActivated(Options opts) throws BadCommandLineException {
        opts.setFieldRendererFactory(new ModernizationFieldRendererFactory(this), this);
    }

    /**
     * Run the add-on.
     *
     * <p>
     * This method is invoked after XJC has internally finished
     * the code generation. Plugins can tweak some of the generated
     * code (or add more code) by using {@link Outline} and {@link Options}.
     *
     * <p>
     * Note that this method is invoked only when a {@link Plugin}
     * is activated.
     * 
     * @param outline
     *      This object allows access to various generated code.
     * 
     * @param errorHandler
     *      Errors should be reported to this handler.
     * 
     * @return
     *      If the add-on executes successfully, return true.
     *      If it detects some errors but those are reported and
     *      recovered gracefully, return false.
     *
     * @throws SAXException
     *      After an error is reported to {@link ErrorHandler}, the
     *      same exception can be thrown to indicate a fatal irrecoverable
     *      error. {@link ErrorHandler} itself may throw it, if it chooses
     *      not to recover from the error.
     */
	@Override
	public boolean run(Outline outline, Options opts, ErrorHandler errorHandler) throws SAXException {
        boolean hasCopyablePlugin = false;
        for (final Plugin p : opts.activePlugins) {
            hasCopyablePlugin |= "org.jvnet.jaxb2_commons.plugin.copyable.CopyablePlugin".equals(p.getClass().getName());
        }

        final JCodeModel codeModel = outline.getCodeModel();
        assert null != codeModel;
        final JClass optional = new JDirectClassEx(codeModel, "java.util.Optional");

        for (final PackageOutline p : outline.getAllPackageContexts()) {
            final JDefinedClass objectFactory = p.objectFactory();
            addDummyFactoryMethod(objectFactory);

            for (final ClassOutline c : p.getClasses()) {
                final JDefinedClass impl = c.implClass;

                final Iterable<FieldSummary> localFields = localFields(opts, c);
                final Iterable<FieldSummary> ancestorFields = ancestorFields(opts, c);

                // If the class or its (generated) superclass has fields, then
                // generate a value constructor
                final boolean doGenerateValueConstructor = nonCollectionFields(localFields).iterator().hasNext() || nonCollectionFields(ancestorFields).iterator().hasNext();
                if (doGenerateValueConstructor) {
                    // Replace the existing object factory method with one that
                    // accepts values for required fields.
                    updateObjectFactory(c, ancestorFields, localFields);

                    // Create the default constructor used by JAXB.
                    addDefaultConstructor(c, localFields);

                    // Create a constructor that initializes all non-collection fields.
                    addConstructor(c, nonCollectionFields(ancestorFields), nonCollectionFields(localFields));

                    final boolean hasRequired = requiredFields(localFields).iterator().hasNext() || requiredFields(ancestorFields).iterator().hasNext();
                    final boolean hasOptional = optionalFields(localFields).iterator().hasNext() || optionalFields(ancestorFields).iterator().hasNext();
                    if (hasRequired && hasOptional) {
                        // Create a constructor that only initializes required fields.
                        addConstructor(c, requiredFields(ancestorFields), requiredFields(localFields));
                    }
                }

                for (final FieldSummary f : nonStaticFields(localFields)) {

                    if (f.def instanceof SingleField) {
                        final JType[] oneArg = { f.type };
                        final JMethod setter = impl.getMethod(f.setterName, oneArg);
                        if (null != setter && !f.isRequired && hasCopyablePlugin && useOptional()) {
                            // Create a setter that takes an optional argument
                            // TODO: Should the new setter be added to the interface?
                            final List<JMethod> methods = (List<JMethod>) impl.methods();
                            final int pos = 1 + methods.indexOf(setter);

                            final JMethod newSetter = impl.method(PUBLIC | FINAL, void.class, f.setterName);
                            final JClass newType = optional.narrow(f.type.boxify());
                            final JVar arg = newSetter.param(newType, "value");
                            for (final JClass annotation : notNullableAnnotations(codeModel)) {
                                arg.annotate(annotation);
                            }
// TODO:                        newSetter.javadoc().add("ADD JAVADOC");
                            newSetter.body().assign(JExpr._this().ref(f.var), arg.invoke("orElse").arg(JExpr._null()));

                            // Move the new method so that it is adjacent to the other setter.
                            methods.remove(newSetter);
                            methods.add(pos, newSetter);
                        }
                    }
                }
            }
        }

        return true;
	}

    @Override
    public @NotNull Iterable<JClass> notNullableAnnotations(final @NotNull JCodeModel codeModel) {
        final ArrayList<JClass> results = new ArrayList<>();

        if (lombok()) {
            results.add(new JDirectClassEx(codeModel, "lombok.NonNull"));
        }
        if (jsr305()) {
            results.add(new JDirectClassEx(codeModel, "javax.annotation.Nonnull"));
        }
        if (jetbrains()) {
            results.add(new JDirectClassEx(codeModel, "org.jetbrains.annotations.NotNull"));
        }
        if (android()) {
            results.add(new JDirectClassEx(codeModel, "android.support.annotation.NonNull"));
        }
        if (jsr303()) {
            results.add(new JDirectClassEx(codeModel, "javax.validation.constraints.NotNull"));
        }
        if (findbugs()) {
            results.add(new JDirectClassEx(codeModel, "edu.umd.cs.findbugs.annotations.NonNull"));
        }

        return results;
    }

    @Override
    public @NotNull Iterable<JClass> nullableAnnotations(final @NotNull JCodeModel codeModel) {
        final ArrayList<JClass> results = new ArrayList<>();

        if (lombok()) {
            // Lombok does not have an annotation representing a nullable type.
        }
        if (jsr305()) {
            results.add(new JDirectClassEx(codeModel, "javax.annotation.Nullable"));
        }
        if (jsr303()) {
            results.add(new JDirectClassEx(codeModel, "javax.validation.constraints.Null"));
        }
        if (jetbrains()) {
            results.add(new JDirectClassEx(codeModel, "org.jetbrains.annotations.Nullable"));
        }
        if (android()) {
            results.add(new JDirectClassEx(codeModel, "android.support.annotation.Nullable"));
        }
        if (findbugs()) {
            results.add(new JDirectClassEx(codeModel, "edu.umd.cs.findbugs.annotations.CheckForNull"));
        }

        return results;
    }

    private void updateObjectFactory(final ClassOutline c, final Iterable<FieldSummary> ancestorFields, final Iterable<FieldSummary> localFields) {
        if (c.target.isAbstract()) return;

        final JCodeModel codeModel = c.implClass.owner();
        assert null != codeModel;
        final JInvocation result = JExpr._new(c.implClass);
        final String methodName = "create" + c.ref.name();
        c._package().objectFactory().methods().remove(c._package().objectFactory().getMethod(methodName, new JType[0]));
        final JMethod fm = c._package().objectFactory().method(PUBLIC | FINAL, c.ref, methodName);
        fm.body()._return(result);

        // Add each argument to the super constructor.
        for (final FieldSummary f : nonStaticFields(requiredFields(ancestorFields))) {
            final JVar arg = fm.param(FINAL, f.type, f.name);
            if (!f.type.isPrimitive()) {
                for (final JClass annotation : f.isRequired ? notNullableAnnotations(codeModel) : nullableAnnotations(codeModel)) {
                    arg.annotate(annotation);
                }
            }
            result.arg(arg);
        }

        // Now add constructor parameters for each field in "this" class, and
        // assign them to our fields.
        for (final FieldSummary f : nonStaticFields(requiredFields(localFields))) {
            final JVar arg = fm.param(FINAL, f.type, f.name);
            result.arg(arg);
            if (f.type.isReference()) {
                for (final JClass annotation : f.isRequired ? notNullableAnnotations(codeModel) : nullableAnnotations(codeModel)) {
                    arg.annotate(annotation);
                }
            }
        }
    }

    private static List<FieldSummary> localFields(Options opts, final ClassOutline c) {
        final List<FieldSummary> localFields = new ArrayList<>(c.getDeclaredFields().length);
        for (final FieldOutline f : c.getDeclaredFields()) {
            if (null == f) continue;
            localFields.add(new FieldSummary(f, opts.enableIntrospection));
        }
        return localFields;
    }

    private static List<FieldSummary> ancestorFields(Options opts, final ClassOutline c) {
        final List<FieldSummary> results = new ArrayList<>();

        ClassOutline ancestor = c.getSuperClass();
        while (null != ancestor) {
            results.addAll(0, localFields(opts, ancestor));
            ancestor = ancestor.getSuperClass();
        }

        return results;
    }

    private void addDummyFactoryMethod(final JDefinedClass objectFactory) {
        final JMethod m = objectFactory.method(STATIC, Object.class, "initialValue");
        final JTypeVar t = m.generify("T");
        final JClass p = objectFactory._package().owner().ref(Object.class);

        final JCodeModel owner = objectFactory.owner();
        assert null != owner;
		for (final JClass annotation : notNullableAnnotations(owner)) {
            m.annotate(annotation);
        }
        m.annotate(SuppressWarnings.class).paramArray("value").param("null").param("unchecked");
        m.type(t);
        m.body()._return(JExpr.cast(t, p.staticRef("class").invoke("cast").arg(JExpr._null())));
    }

//    if (fieldIsRequired) {
//        // TODO: Assumes that there is only one constructor.
//        final Iterator<JMethod> iterator = context.implClass.constructors();
//        final JMethod constructor = iterator.hasNext() ? iterator.next() : context.implClass.constructor(PUBLIC);
//        final JVar parameter = constructor.param(exposedType, field.name());
//        if (!exposedType.isPrimitive()) {
//            for (final JClass annotation : configuration().notNullableAnnotations(codeModel)) {
//                parameter.annotate(annotation);
//            }
//        }
//        constructor.body().assign(fieldRef, requireNotNull(codeModel, castToImplType(implType, exposedType, parameter), parameter.type().isReference() && fieldIsRequired));
//    }

    private void addConstructor(final ClassOutline c, final Iterable<FieldSummary> ancestorFields, final Iterable<FieldSummary> localFields) {

        final JDefinedClass impl = c.implClass;
        final JCodeModel codeModel = impl.owner();
        assert null != codeModel;

        // Create the skeleton of the value constructor
        final JMethod constructor = impl.constructor(c.target.isAbstract() ? PROTECTED : PUBLIC);
        constructor.javadoc().add("initializing value constructor");

        // If our superclass is also being generated, then we can assume it will
        // also have its own value constructor, so we add an invocation of that
        // constructor.
        if (impl._extends() instanceof JDefinedClass) {

            final JInvocation superInvocation = constructor.body().invoke("super");

            // Add each argument to the super constructor.
            for (final FieldSummary f : nonStaticFields(ancestorFields)) {
                final JVar arg = constructor.param(FINAL, f.type, f.name);
                if (!f.type.isPrimitive()) {
                    for (final JClass annotation : f.isRequired ? notNullableAnnotations(codeModel) : nullableAnnotations(codeModel)) {
                        arg.annotate(annotation);
                    }
                }
                superInvocation.arg(arg);
            }
        }

        // Now add constructor parameters for each field in "this" class, and
        // assign them to our fields.
        for (final FieldSummary f : nonStaticFields(localFields)) {
            final JVar arg = constructor.param(FINAL, f.type, f.name);
            boolean isReference = f.type.isReference();
            final JExpression rhs = isReference && f.isRequired ? codeModel.ref(Objects.class).staticInvoke("requireNonNull").arg(arg).arg(lit(f.name + " cannot be null")) : arg;
            constructor.body().assign(JExpr.refthis(f.name), rhs);
            if (isReference) {
                for (final JClass annotation : f.isRequired ? notNullableAnnotations(codeModel) : nullableAnnotations(codeModel)) {
                    arg.annotate(annotation);
                }
            }
        }
    }

    private static void addDefaultConstructor(final ClassOutline c, final Iterable<FieldSummary> localFields) {
        final JMethod defaultConstructor = c.implClass.constructor(NONE);
        defaultConstructor.javadoc().add(""
            + "Only used by JAXB\n"
            + "<p>\n"
            + "  JAXB instantiates an object using the class's default constructor.\n"
            + "  Unfortunately, this leaves required fields in an invalid state.\n"
            + "  To circumvent this, we create a package-visible, default constructor\n"
            + "  that initializes each required (i.e., final) field with a dummy,\n"
            + "  invalid value. After creation, JAXB initializes each field with the\n"
            + "  value present in the XML. We assume that JAXB will raise an exception\n"
            + "  if a required value is missing or is an invalid type. End the end, we\n"
            + "  should have a properly constructed object.\n"
            + "</p>"
        );
        final JBlock body = defaultConstructor.body();
        body.invoke("super");

        // Now add constructor parameters for each field in "this" class, and
        // assign them to our fields.
        for (final FieldSummary field : localFields) {
            if (field.isRequired) {
                body.assign(JExpr.refthis(field.name), initialValue(c, field.type));
            }
        }
    }

    private static JExpression initialValue(final ClassOutline c, final JType type) {
        if (type.isReference()) {
            return c._package().objectFactory().staticInvoke("initialValue");
        } else if (c.parent().getCodeModel().BOOLEAN.equals(type)) {
            return JExpr.lit(false);
        } else {
            return JExpr.lit(0);
        }
    }

    private static Iterable<FieldSummary> nonCollectionFields(final Iterable<FieldSummary> o) {
        return new Iterable<FieldSummary>() {
            @Override
            public Iterator<FieldSummary> iterator() {
                return new FilteredIterator<FieldSummary>(o.iterator()) {
                    @Override
                    protected boolean isMatch(FieldSummary f) {
                        return !f.def.getPropertyInfo().isCollection();
                    }
                };
            }
        };
    }

    // TODO: Determine what should be done for static fields.
    private static Iterable<FieldSummary> nonStaticFields(final Iterable<FieldSummary> o) {
        return new Iterable<FieldSummary>() {
            @Override
            public Iterator<FieldSummary> iterator() {
                return new FilteredIterator<FieldSummary>(o.iterator()) {
                    @Override
                    protected boolean isMatch(FieldSummary f) {
                        return !f.isStaticField;
                    }
                };
            }
        };
    }

    private static Iterable<FieldSummary> optionalFields(final Iterable<FieldSummary> o) {
        return new Iterable<FieldSummary>() {
            @Override
            public Iterator<FieldSummary> iterator() {
                return new FilteredIterator<FieldSummary>(o.iterator()) {
                    @Override
                    protected boolean isMatch(FieldSummary f) {
                        return !f.isRequired && !f.def.getPropertyInfo().isCollection();
                    }
                };
            }
        };
    }

    private static Iterable<FieldSummary> requiredFields(final Iterable<FieldSummary> o) {
        return new Iterable<FieldSummary>() {
            @Override
            public Iterator<FieldSummary> iterator() {
                return new FilteredIterator<FieldSummary>(o.iterator()) {
                    @Override
                    protected boolean isMatch(FieldSummary f) {
                        return f.isRequired;
                    }
                };
            }
        };
    }

}
