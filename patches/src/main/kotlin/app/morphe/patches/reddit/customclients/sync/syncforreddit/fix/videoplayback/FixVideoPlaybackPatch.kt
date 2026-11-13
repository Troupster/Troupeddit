/*
 * Cause analysis: https://github.com/TheLastWallaby/revanced-redditsync/issues/74
 */
package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.videoplayback

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/morphe/extension/syncforreddit/RedditVideoPlaybackExtension;"
private const val PLAYBACK_SOURCE_METHOD = "playbackSource(Ljava/lang/String;)Ljava/lang/String;"

@Suppress("unused")
val fixRedditVideoPlaybackPatch = bytecodePatch(
    name = "Fix Reddit video playback",
    description = "Fixes \"Could not load video\" for Reddit-hosted videos. Reddit now rejects the unsigned HLS " +
        "playlist Sync requests, so the video is opened through its DASH manifest instead.",
    default = true,
) {
    dependsOn(ExtensionPatches.Sync)
    compatibleWith(*AppCompatibility.SyncForReddit)

    execute {
        // CustomExoPlayerView.setSource(String, boolean, boolean): p1 is the video URL.
        PlayerSetSourceFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->$PLAYBACK_SOURCE_METHOD
                move-result-object p1
            """
        )

        // Static media source helper used by feed autoplay: p0 is the video URL.
        AutoplayMediaSourceFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->$PLAYBACK_SOURCE_METHOD
                move-result-object p0
            """
        )
    }
}
