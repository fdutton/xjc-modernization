package fgd.tools.extensions.xjc;

import com.sun.codemodel.internal.JExpression;
import com.sun.tools.internal.xjc.BadCommandLineException;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.xml.internal.xsom.XmlString;

public final class JSR310Plugin extends JSR310AbstractPlugin {

    @Override
    public void onActivated(Options opts) throws BadCommandLineException {
        replace("date", new NTypeUse("java.time.LocalDate") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.LocalDate");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("dateTime", new NTypeUse("java.time.Instant") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.Instant");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("duration", new NTypeUse("java.time.Period") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.Period");
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
        replace("gMonth", new NTypeUse("java.time.Month") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.MonthDay");
                return c.staticInvoke("parse").arg(lexical.value.trim() + "-01").invoke("getMonth");
            }
        });
        replace("gMonthDay", new NTypeUse("java.time.MonthDay") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.MonthDay");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("gYear", new NTypeUse("java.time.Year") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.Year");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("gYearMonth", new NTypeUse("java.time.YearMonth") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.YearMonth");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
        replace("time", new NTypeUse("java.time.LocalTime") {
            @Override
            public JExpression createConstant(Outline outline, XmlString lexical) {
                final JDirectClassEx c = new JDirectClassEx(outline.getCodeModel(), "java.time.LocalTime");
                return c.staticInvoke("parse").arg(lexical.value.trim());
            }
        });
    }

    @Override
    public String getOptionName() {
        return "JSR310";
    }

    @Override
    public String getUsage() {
        return "  -JSR310            :  use Java 8 Date and Time API instead of XMLGregorianCalendar";
    }

}
