package app.gamenative.displayresize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DisplaySizeParser {
    private static final Pattern PHYSICAL = Pattern.compile(
            "(?m)^\\s*Physical size:\\s*(\\d+)x(\\d+)\\s*$");
    private static final Pattern OVERRIDE = Pattern.compile(
            "(?m)^\\s*Override size:\\s*(\\d+)x(\\d+)\\s*$");

    private DisplaySizeParser() {}

    public static DisplaySizeSnapshot parse(String output) {
        Matcher physicalMatcher = PHYSICAL.matcher(output == null ? "" : output);
        if (!physicalMatcher.find()) {
            throw new IllegalArgumentException("无法读取物理分辨率");
        }

        DisplayDimensions physical = dimensionsFrom(physicalMatcher);
        Matcher overrideMatcher = OVERRIDE.matcher(output);
        DisplayDimensions override = overrideMatcher.find() ? dimensionsFrom(overrideMatcher) : null;
        return new DisplaySizeSnapshot(physical, override);
    }

    private static DisplayDimensions dimensionsFrom(Matcher matcher) {
        try {
            return new DisplayDimensions(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("无法读取物理分辨率", error);
        }
    }
}
