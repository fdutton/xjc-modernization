package fgd.tools.extensions.xjc;

import com.sun.codemodel.internal.JExpression;
import com.sun.tools.internal.xjc.BadCommandLineException;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.xml.internal.xsom.XmlString;

public final class JSR310BackportPlugin extends JSR310AbstractPlugin {

    @Override
    public void onActivated(Options opts) throws BadCommandLineException {
        replace("date", new NTypeUse("org.threeten.bp.LocalDate") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.LocalDate");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("dateTime", new NTypeUse("org.threeten.bp.Instant") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.Instant");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("duration", new NTypeUse("org.threeten.bp.Period") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.Period");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("gDay", new NTypeUse("java.lang.Integer") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.lang.Integer");
                return c.staticInvoke("valueOf").arg(lexical.value.trim().substring(3)); // value is prefaced with three hyphens 
            }
        });
        replace("gMonth", new NTypeUse("org.threeten.bp.Month") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.MonthDay");
                return c.staticInvoke("parse").arg(lexical.value.trim() + "-01").invoke("getMonth");
            }
        });
        replace("gMonthDay", new NTypeUse("org.threeten.bp.MonthDay") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.MonthDay");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("gYear", new NTypeUse("org.threeten.bp.Year") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.Year");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("gYearMonth", new NTypeUse("org.threeten.bp.YearMonth") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.YearMonth");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("time", new NTypeUse("org.threeten.bp.LocalTime") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "org.threeten.bp.LocalTime");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
    }

    @Override
    public String getOptionName() {
        return "JSR310bp";
    }

    @Override
    public String getUsage() {
        return "  -JSR310bp          :  use a backport of the Java 8 Date and Time API in Java 7";
    }

}
