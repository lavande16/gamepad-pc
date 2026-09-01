package app.gamenative.ui.screen.settings

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import app.gamenative.R

/**
 * Mechanical host for the GamePad About content.
 *
 * The shared Compose settings header owns navigation; this wrapper hosts only the page body.
 */
@Composable
internal fun AboutPage(
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            inflateGameFoldNsAbout(context).apply {
                setLinkClickListener(R.id.button_website, R.string.website_link)
                setLinkClickListener(R.id.button_xiaohongshu, R.string.xiaohongshu_link)
                setLinkClickListener(R.id.button_feedback, R.string.dribbble_link)
                setLinkClickListener(R.id.button_dribbble, R.string.dribbble_link)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

internal fun inflateGameFoldNsAbout(context: Context): View {
    val themedContext = ContextThemeWrapper(context, R.style.Theme_GameFoldNsAboutHost)
    return LayoutInflater.from(themedContext).inflate(R.layout.fragment_about, null, false)
}

private fun View.setLinkClickListener(viewId: Int, linkId: Int) {
    findViewById<View>(viewId).setOnClickListener {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(linkId))))
    }
}
