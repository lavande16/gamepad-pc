package app.gamenative.gamepad

import kotlin.math.min

object GamePadControllerViewportPolicy {
    private const val DESIGN_WIDTH = 2304f
    private const val DESIGN_HEIGHT = 1440f
    private const val FULL_HD_WIDTH = 1920
    private const val FULL_HD_HEIGHT = 1080
    private const val SIXTEEN_NINE_WIDTH = 2304
    private const val SIXTEEN_NINE_HEIGHT = 1296
    private const val SIXTEEN_NINE_VERTICAL_OFFSET = -72f

    fun fit(windowWidth: Int, windowHeight: Int): GamePadTransform {
        require(windowWidth > 0 && windowHeight > 0)
        if (windowWidth == FULL_HD_WIDTH && windowHeight == FULL_HD_HEIGHT) {
            return GamePadTransform(
                scale = 0.82f,
                translateX = 16f,
                translateY = -44f,
            )
        }
        if (isSixteenNineTarget(windowWidth, windowHeight)) {
            return nativeScaleSixteenNineTransform()
        }
        return centeredFit(windowWidth, windowHeight)
    }

    fun homeFit(windowWidth: Int, windowHeight: Int): GamePadTransform {
        require(windowWidth > 0 && windowHeight > 0)
        if (windowWidth == FULL_HD_WIDTH && windowHeight == FULL_HD_HEIGHT) {
            return GamePadTransform(
                scale = 0.75f,
                translateX = 144f,
                translateY = 0f,
            )
        }
        if (isSixteenNineTarget(windowWidth, windowHeight)) {
            return nativeScaleSixteenNineHomeTransform()
        }
        return centeredFit(windowWidth, windowHeight)
    }

    /**
     * Keep the approved 2304x1440 controller artwork at 1:1 on the 16:9 target.
     * The target viewport keeps the native 1:1 artwork size and horizontal origin. The
     * 16:9 layout moves the lower controller artwork up by 72px to account for the
     * shorter active display area. The home/logo layer keeps its native Y origin so it
     * sits 72px lower than the previous baseline while retaining the same 1:1 scale.
     */
    private fun nativeScaleSixteenNineTransform(): GamePadTransform = GamePadTransform(
        scale = 1f,
        translateX = 0f,
        translateY = SIXTEEN_NINE_VERTICAL_OFFSET,
    )

    private fun nativeScaleSixteenNineHomeTransform(): GamePadTransform = GamePadTransform(
        scale = 1f,
        translateX = 0f,
        translateY = 0f,
    )

    private fun isSixteenNineTarget(windowWidth: Int, windowHeight: Int): Boolean =
        windowWidth == SIXTEEN_NINE_WIDTH && windowHeight == SIXTEEN_NINE_HEIGHT

    private fun centeredFit(windowWidth: Int, windowHeight: Int): GamePadTransform {
        val scale = min(windowWidth / DESIGN_WIDTH, windowHeight / DESIGN_HEIGHT)
        return GamePadTransform(
            scale = scale,
            translateX = (windowWidth - DESIGN_WIDTH * scale) / 2f,
            translateY = (windowHeight - DESIGN_HEIGHT * scale) / 2f,
        )
    }
}
