package fgd.tools.extensions.xjc;

import static com.sun.codemodel.internal.JMod.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import javax.validation.constraints.NotNull;

import org.xml.sax.InputSource;

import com.sun.codemodel.internal.JMods;

public abstract class ModernizationTests {

    public static boolean isFinal(final JMods mods) {
        return 0 != ((STATIC | FINAL) & mods.getValue());
    }

    public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
        return new IteratorWrapper<T>(iterator);
    }

    @NotNull
    public static InputSource sample(final @NotNull String xmlSchema) {
        final Reader reader = new UncloseableStringReader(xmlSchema);
        final InputSource result = new InputSource(reader);
        result.setSystemId("sample.xsd");
        result.setEncoding("UTF-16");
        return result;
    }

    private static class IteratorWrapper<T> implements Iterable<T> {

        private final Iterator<T> iterator;

        IteratorWrapper(final Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<T> iterator() {
            return this.iterator;
        }
        
    }

    private static class UncloseableStringReader extends StringReader {

        UncloseableStringReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            try {
                // XJC actually reopens the schema file several times.
                // To simulate this, do not actually close the reader.
                // Just reset it to the beginning of the buffer.
                reset();
            } catch (IOException e) {
                // Should never happen
                e.printStackTrace();
            }
        }
    }

}
