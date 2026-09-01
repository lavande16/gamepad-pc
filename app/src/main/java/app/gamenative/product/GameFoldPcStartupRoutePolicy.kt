package app.gamenative.product

enum class GameFoldPcStartupRoute {
    READY,
    PERMISSION_REQUIRED,
    UNSUPPORTED_MTK,
    UNSUPPORTED_ABI,
}

object GameFoldPcStartupRoutePolicy {
    fun resolve(
        supportedAbis: Array<String>,
        socModel: String?,
        hardware: String?,
        hasStorageAccess: Boolean,
    ): GameFoldPcStartupRoute = when {
        GameFoldPcProcessorPolicy.isMediaTek(socModel, hardware) ->
            GameFoldPcStartupRoute.UNSUPPORTED_MTK

        !GameFoldPcProcessorPolicy.isSupported(supportedAbis) ->
            GameFoldPcStartupRoute.UNSUPPORTED_ABI

        !hasStorageAccess -> GameFoldPcStartupRoute.PERMISSION_REQUIRED
        else -> GameFoldPcStartupRoute.READY
    }
}
