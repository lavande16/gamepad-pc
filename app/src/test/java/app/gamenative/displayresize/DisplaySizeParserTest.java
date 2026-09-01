package app.gamenative.displayresize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DisplaySizeParserTest {
    @Test
    public void physicalSizeWithoutOverrideIsTheDefaultState() {
        DisplaySizeSnapshot snapshot = DisplaySizeParser.parse(
                "Physical size: 1440x2304\n");

        assertEquals(new DisplayDimensions(1440, 2304), snapshot.physical());
        assertFalse(snapshot.hasOverride());
    }

    @Test
    public void exactForcedSizeIsRecognizedAsTarget() {
        DisplaySizeSnapshot snapshot = DisplaySizeParser.parse(
                "Physical size: 1440x2304\nOverride size: 1296x2304\n");

        assertTrue(snapshot.isTargetOverride());
        assertEquals(new DisplayDimensions(1296, 2304), snapshot.current());
    }

    @Test
    public void malformedOutputIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> DisplaySizeParser.parse("unexpected output"));

        assertEquals("无法读取物理分辨率", error.getMessage());
    }
}
