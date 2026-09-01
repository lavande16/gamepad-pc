package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Test

class GameFoldPcStartupRoutePolicyTest {
    @Test
    fun mtkTakesPriorityOverMissingStoragePermission() {
        assertEquals(
            GameFoldPcStartupRoute.UNSUPPORTED_MTK,
            GameFoldPcStartupRoutePolicy.resolve(
                supportedAbis = arrayOf("arm64-v8a"),
                socModel = "Dimensity 9300",
                hardware = "mt6989",
                hasStorageAccess = false,
            ),
        )
    }

    @Test
    fun nonArm64IsRejectedBeforePermission() {
        assertEquals(
            GameFoldPcStartupRoute.UNSUPPORTED_ABI,
            GameFoldPcStartupRoutePolicy.resolve(
                supportedAbis = arrayOf("x86_64", "x86"),
                socModel = "",
                hardware = "ranchu",
                hasStorageAccess = false,
            ),
        )
    }

    @Test
    fun supportedDeviceWithoutStoragePermissionNeedsPermission() {
        assertEquals(
            GameFoldPcStartupRoute.PERMISSION_REQUIRED,
            GameFoldPcStartupRoutePolicy.resolve(
                supportedAbis = arrayOf("arm64-v8a"),
                socModel = "SM8650",
                hardware = "qcom",
                hasStorageAccess = false,
            ),
        )
    }

    @Test
    fun supportedAuthorizedDeviceIsReady() {
        assertEquals(
            GameFoldPcStartupRoute.READY,
            GameFoldPcStartupRoutePolicy.resolve(
                supportedAbis = arrayOf("arm64-v8a"),
                socModel = "SM8650",
                hardware = "qcom",
                hasStorageAccess = true,
            ),
        )
    }
}
