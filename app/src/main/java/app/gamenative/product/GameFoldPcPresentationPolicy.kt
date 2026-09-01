package app.gamenative.product

object GameFoldPcPresentationPolicy {
    data class Presentation(
        val bootSplashBrand: String,
        val showBootSplashFixedTips: Boolean,
        val showIconStylePicker: Boolean,
    )

    fun forProduct(localShell: Boolean): Presentation = if (localShell) {
        Presentation(
            bootSplashBrand = GameFoldPcDefaults.BOOT_SPLASH_BRAND,
            showBootSplashFixedTips = GameFoldPcDefaults.SHOW_BOOT_SPLASH_FIXED_TIPS,
            showIconStylePicker = GameFoldPcDefaults.SHOW_ICON_STYLE_PICKER,
        )
    } else {
        Presentation(
            bootSplashBrand = "GameNative",
            showBootSplashFixedTips = true,
            showIconStylePicker = true,
        )
    }
}
