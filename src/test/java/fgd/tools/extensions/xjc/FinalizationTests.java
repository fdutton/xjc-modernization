package fgd.tools.extensions.xjc;

import static com.sun.tools.internal.xjc.Language.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xml.sax.InputSource;

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JPackage;
import com.sun.tools.internal.xjc.ModelLoader;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.outline.Outline;

public class FinalizationTests extends ModernizationTests {

    @Test
    public void testFinalizeMethods() throws Exception {
        Options options = new Options();
        options.setSchemaLanguage(XMLSCHEMA);  // disable auto-guessing
        options.addGrammar(new InputSource("src/test/resources/sample.xsd"));
        options.compatibilityMode = Options.EXTENSION;

        final ModernizationPlugin modernizationPlugin = new ModernizationPlugin();
        options.activePlugins.add(modernizationPlugin);
        modernizationPlugin.parseArgument(options, new String[] { "-finalize-methods" }, 0);
        modernizationPlugin.onActivated(options);

        final MessageCollector receiver = new MessageCollector();

        Model model = ModelLoader.load(options, new JCodeModel(), receiver);
        assertFalse(receiver.hadErrors());
        assertNotNull(model);

        Outline outline = model.generateCode(options, receiver);
        assertFalse(receiver.hadErrors());
        assertNotNull(outline);

        for (final JPackage p : iterable(model.codeModel.packages())) {
            for (final JDefinedClass c : iterable(p.classes())) {
                for (final JMethod method : c.methods()) {
                    assertTrue("method not final: " + method.name(), isFinal(method.mods()));
                }
            }
        }
    }

}
