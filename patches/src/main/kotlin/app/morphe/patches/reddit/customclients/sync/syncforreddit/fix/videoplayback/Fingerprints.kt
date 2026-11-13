/*
 * Cause analysis: https://github.com/TheLastWallaby/revanced-redditsync/issues/74
 */
package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.videoplayback

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val DASH_FACTORY = "Lcom/google/android/exoplayer2/source/dash/DashMediaSource\$Factory;"
private const val HLS_FACTORY = "Lcom/google/android/exoplayer2/source/hls/HlsMediaSource\$Factory;"

private fun Method.callsCreateMediaSourceOn(factory: String): Boolean =
    implementation?.instructions?.any { instruction ->
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
        reference != null && reference.definingClass == factory && reference.name == "createMediaSource"
    } == true

// Full-screen viewer, pager and long-press peek: setSource stores the URL, then starts the player.
internal object PlayerSetSourceFingerprint : Fingerprint(
    definingClass = "Lcom/laurencedawson/reddit_sync/ui/views/video/CustomExoPlayerView;",
    name = "setSource",
    parameters = listOf("Ljava/lang/String;", "Z", "Z"),
    returnType = "V",
)

// Feed autoplay: a static helper that builds the media source (DASH, HLS or progressive) from a URL.
internal object AutoplayMediaSourceFingerprint : Fingerprint(
    parameters = listOf("Ljava/lang/String;"),
    returnType = "Lcom/google/android/exoplayer2/source/MediaSource;",
    custom = { method, _ ->
        AccessFlags.STATIC.isSet(method.accessFlags) &&
            method.callsCreateMediaSourceOn(DASH_FACTORY) &&
            method.callsCreateMediaSourceOn(HLS_FACTORY)
    }
)
