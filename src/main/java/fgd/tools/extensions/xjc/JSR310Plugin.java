package fgd.tools.extensions.xjc;

public final class JSR310Plugin extends JSR310AbstractPlugin {

	public JSR310Plugin() {
        super(
            new NTypeUse("java.time.Duration"),
            new NTypeUse("java.time.Instant"),
            new NTypeUse("java.time.Period")
        );
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
