/*
 * Reddit now returns HTTP 403 for unsigned v.redd.it HLS playlists. Sync rebuilds exactly that
 * URL (https://v.redd.it/<id>/HLSPlaylist.m3u8) for every Reddit video, so playback fails with
 * "COULD NOT LOAD VIDEO". The DASH manifest for the same video is still served unsigned, and
 * Sync's player already knows how to play a v.redd.it .mpd source.
 *
 * Cause analysis: https://github.com/TheLastWallaby/revanced-redditsync/issues/74
 */
package app.morphe.extension.syncforreddit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class RedditVideoPlaybackExtension {
    private static final Pattern HLS_URL = Pattern.compile(
            "^https?://v\\.redd\\.it/([A-Za-z0-9]{1,32})/HLSPlaylist\\.m3u8(?:\\?.*)?$",
            Pattern.CASE_INSENSITIVE);

    private RedditVideoPlaybackExtension() {
    }

    /**
     * Returns the DASH manifest URL for a v.redd.it HLS playlist URL, or the input unchanged
     * for any other source. Never throws.
     */
    public static String playbackSource(String source) {
        try {
            if (source == null || source.length() > 4096) return source;
            Matcher matcher = HLS_URL.matcher(source);
            if (!matcher.matches()) return source;
            return "https://v.redd.it/" + matcher.group(1) + "/DASHPlaylist.mpd";
        } catch (RuntimeException e) {
            return source;
        }
    }
}
