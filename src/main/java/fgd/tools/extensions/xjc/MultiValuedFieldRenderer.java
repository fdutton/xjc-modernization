package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JExpr.*;
import static com.sun.codemodel.internal.JMod.*;
import static com.sun.tools.internal.xjc.outline.Aspect.*;
import static fgd.tools.extensions.xjc.XJCUtils.*;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldRef;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JOp;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.generator.bean.BeanGenerator;
import com.sun.tools.internal.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.internal.xjc.model.CPropertyInfo;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.outline.FieldOutline;
import com.sun.xml.internal.bind.api.impl.NameConverter;

public final class MultiValuedFieldRenderer extends AbstractFieldRenderer {

    public MultiValuedFieldRenderer(final @NotNull Configuration configuration) {
        super(configuration);
    }

    /**
     * Generates accesssors and fields for the given implementation class, then
     * return {@link FieldOutline} for accessing the generated field.
     */
    @Override
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo propertyInfo) {
        final BeanGenerator generator = context.parent();
        final Model model = generator.getModel();
        final JCodeModel codeModel = generator.getCodeModel();

        final JType memberType = getType(context, propertyInfo, EXPOSED);
        final JClass exposedType = codeModel.ref(List.class).narrow(memberType.boxify());

        final JFieldVar field = emitField(context, propertyInfo, exposedType, memberType);
        final JFieldRef fieldRef = _this().ref(field);

        final AugmentedMethodWriter writer = AugmentedMethodWriter.create(context, configuration(), false, true);
        final NameConverter nc = model.getNameConverter();

        // [RESULT]
        // List getXXX() {
        //     return <ref>;
        // }
        final String getterName = "get"+propertyInfo.getName(true);
        JMethod getter = writer.declareMethod(exposedType,getterName);
        writer.javadoc().append(propertyInfo.javadoc);
        JBlock block = getter.body();
        block._return(JOp.cond(
            JExpr._null().ne(fieldRef),
            fieldRef,
            parenthesis(fieldRef.assign(JExpr._new(codeModel.ref(ArrayList.class).narrow(memberType))))
        ));

        String pname = nc.toVariableName(propertyInfo.getName(true));
        writer.javadoc().append(
            "Gets the value of the "+pname+" property.\n\n"+
            "<p>\n" +
            "This accessor method returns a reference to the live list,\n" +
            "not a snapshot. Therefore any modification you make to the\n" +
            "returned list will be present inside the JAXB object.\n" +
            "This is why there is not a <CODE>set</CODE> method for the " +pname+ " property.\n" +
            "\n"+
            "<p>\n" +
            "For example, to add a new item, do as follows:\n"+
            "<pre>\n"+
            "   "+getterName+"().add(newItem);\n"+
            "</pre>"
        );

// TODO: Add documentation to multi-valued lists.
//        final String documentation = XJCUtils.extractDocumentation(propertyInfo);
//        if (documentation.isEmpty()) {
//            writer.javadoc().addReturn().append("possible object is\n").append(listPossibleTypes(generator, propertyInfo));
//        } else {
//            writer.javadoc().addReturn().append(documentation);
//        }

        // There are two entries for each type: the name and a new line.
        final List<Object> possibleTypes = listPossibleTypes(generator, propertyInfo);
        if (2 < possibleTypes.size()) {
            writer.javadoc()
                .append("\n<p>\nObjects of the following type(s) are allowed in the list\n")
                .append(possibleTypes)
                ;
        }

        return new ModernizationFieldOutline(context, propertyInfo, field, exposedType);
    }

    private final JFieldVar emitField(ClassOutlineImpl context, CPropertyInfo propertyInfo, JType exposedType, JType memberType) {
        final int visibility = configuration().privatizeFields() ? PRIVATE : PROTECTED;
        final JFieldVar field = context.implClass.field(visibility, exposedType, propertyInfo.getName(false));
        final JCodeModel owner = context.implClass.owner();
        assert null != owner;
		for (final JClass annotation : configuration().nullableAnnotations(owner)) {
            field.annotate(annotation);
        }
        annotate(context, propertyInfo, field, memberType);
        return field;
    }

}
