package fgd.tools.extensions.xjc;

import static org.junit.Assert.*;
import static com.sun.tools.internal.xjc.Language.*;

import org.junit.Test;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JPackage;
import com.sun.tools.internal.xjc.ModelLoader;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.outline.Outline;

public class ExampleTests extends ModernizationTests {

	@Test
	public void test() throws Exception {
		Options options = new Options();
		options.setSchemaLanguage(XMLSCHEMA);  // disable auto-guessing
		options.addGrammar(sample(""
            + "<?xml version='1.0' encoding='UTF-16'?>"
            + "<xs:schema"
            + " xmlns='http://foo.com/sample'"
            + " xmlns:xs='http://www.w3.org/2001/XMLSchema'"
            + " elementFormDefault='qualified'"
            + " attributeFormDefault='unqualified'"
            + " targetNamespace='http://foo.com/sample'"
            + ">"
            + "  <xs:complexType name='CurrencyAmount'>"
            + "    <xs:annotation>"
            + "      <xs:documentation>It provides a monetary amount and the currency code to reflect the currency in which this amount is expressed.</xs:documentation>"
            + "    </xs:annotation>"
            + "    <xs:attribute name='amount' type='xs:decimal' use='required'>"
            + "      <xs:annotation>"
            + "        <xs:documentation>A monetary amount.</xs:documentation>"
            + "      </xs:annotation>"
            + "    </xs:attribute>"
            + "    <xs:attribute name='currencyCode' use='required'>"
            + "      <xs:annotation>"
            + "        <xs:documentation>The code that specifies a monetary unit. Use ISO 4217, three alpha code.</xs:documentation>"
            + "      </xs:annotation>"
            + "      <xs:simpleType>"
            + "        <xs:restriction base='xs:string'>"
            + "          <xs:length value='3'/>"
            + "        </xs:restriction>"
            + "      </xs:simpleType>"
            + "    </xs:attribute>"
            + "  </xs:complexType>"
            + "</xs:schema>"
        ));

        final MessageCollector receiver = new MessageCollector();

		Model model = ModelLoader.load(options, new JCodeModel(), receiver);
		assertFalse(receiver.hadErrors());
		assertNotNull(model);

		Outline outline = model.generateCode(options, receiver);
		assertFalse(receiver.hadErrors());
		assertNotNull(outline);

		JPackage p = model.codeModel.packages().next();
        assertNotNull(p);
		assertEquals("com.foo.sample", p.name());

		JDefinedClass c = p.classes().next();
        assertNotNull(c);
        assertEquals("CurrencyAmount", c.name());
	}

}
