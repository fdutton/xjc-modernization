package fgd.tools.extensions.xjc;

public final class JSR310BackportPlugin extends JSR310AbstractPlugin {

	public JSR310BackportPlugin() {
        super(
            new NTypeUse("org.threeten.bp.Duration"),
            new NTypeUse("org.threeten.bp.Instant"),
            new NTypeUse("org.threeten.bp.Period")
        );
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
