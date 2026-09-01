package com.winlator.xserver.extensions;

/**
 * Identifies Wine's XInput2 master-pointer initialization subscription. Wine selects
 * XI_DeviceChanged on XIAllMasterDevices before enabling raw input; in a virtual desktop
 * it can omit XI_RawMotion on the desktop window. The gyro-only transport uses this
 * signature as a narrow fallback without changing normal pointer event routing.
 */
public final class XInput2RawMotionRoutingPolicy {
    private static final int XI_ALL_DEVICES = 0;
    private static final int XI_ALL_MASTER_DEVICES = 1;
    private static final int MASTER_POINTER_ID = 2;

    private XInput2RawMotionRoutingPolicy() {}

    public static boolean shouldUseWineFallback(
            int selectedDeviceId,
            boolean deviceChangedSelected,
            int motionDeviceId) {
        if (!deviceChangedSelected || motionDeviceId != MASTER_POINTER_ID) return false;
        return selectedDeviceId == XI_ALL_DEVICES
                || selectedDeviceId == XI_ALL_MASTER_DEVICES
                || selectedDeviceId == motionDeviceId;
    }
}
