package fgd.tools.extensions.xjc;

import java.util.ArrayList;

import org.xml.sax.SAXParseException;

import com.sun.tools.internal.xjc.AbortException;
import com.sun.tools.internal.xjc.ConsoleErrorReporter;

public final class MessageCollector extends ConsoleErrorReporter {

    private final ArrayList<SAXParseException> errors;
    private final ArrayList<SAXParseException> warnings;
    private final ArrayList<SAXParseException> messages;

    public MessageCollector() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    @Override
    public void warning(SAXParseException e) throws AbortException {
        super.warning(e);
        this.warnings.add(e);
    }

    @Override
    public void info(SAXParseException e) {
        super.info(e);
        this.messages.add(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws AbortException {
        super.fatalError(e);
        this.errors.add(e);
    }

    @Override
    public void error(SAXParseException e) throws AbortException {
        super.error(e);
        this.errors.add(e);
    }

    public boolean hadErrors() {
        return !this.errors.isEmpty();
    }

    public boolean hadWarnings() {
        return !this.warnings.isEmpty();
    }

    public boolean hadMessages() {
        return !this.messages.isEmpty();
    }
}