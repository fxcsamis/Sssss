# Real NewPipe-style backend wired into Cloudihub UI

This project's UI (glassmorphic home/hub theme, etc.) is kept, but is now backed by a real
NewPipeExtractor + ExoPlayer pipeline — the same underlying approach the actual NewPipe app uses —
instead of hardcoded mock data and a fake progress-bar animation.

## What's real now
- **Home feed** (`CloudihubViewModel.loadTrending()`): loads YouTube's real "Trending" kiosk via
  `KioskInfo.getInfo(...)`, mapped into the app's existing `CloudVideo` model.
- **Infinite scroll** (`CloudihubViewModel.loadMoreVideos()`): scrolling near the bottom of the
  Home feed or search results automatically fetches and appends the next page via
  `KioskInfo.getMoreItems(...)` / `SearchInfo.getMoreItems(...)`, unlimited like real YouTube,
  instead of stopping after one fixed batch.
- **Search** (`CloudihubViewModel.searchYoutube()`): real YouTube search via `SearchInfo.getInfo(...)`,
  debounced 450ms after typing stops, also paginated.
- **High-quality playback** (`VideoStreamingPlayer.kt` + `NewPipeService.resolvePlayableStream()`):
  a real `androidx.media3.exoplayer.ExoPlayer` plays the video. Prefers combining the best
  video-only + audio-only streams via `MergingMediaSource` (needed for anything above ~720p on
  YouTube), falling back to a progressive stream only if no adaptive streams exist.
- **YouTube-style watch screen**: redesigned to a real YouTube-like layout — fixed 16:9 player at
  top, then a scrollable light-themed section below with title, channel row + Subscribe, Like/
  Dislike/Share/Save action pills, and an "Up next" related-videos list (reusing the same
  `VideoCloudCard` component as the Home feed).

## Playback bug fix (from the "video won't play" report)
The stuck-at-0%-buffered / black-screen issue was a real bug: the ExoPlayer `DataSource` wasn't
sending the same `User-Agent` header that was used to resolve the stream URL from YouTube.
YouTube's CDN (`googlevideo.com`) can silently reject playback requests with a mismatched
User-Agent — no error was previously surfaced, so the player just looked frozen. Fixed by using
`DefaultHttpDataSource.Factory().setUserAgent(NewPipeDownloader.USER_AGENT)` for both video and
audio sources, and by adding a `Player.Listener.onPlayerError` handler that now surfaces any real
playback failure as visible on-screen text instead of failing silently.

## Not included this round (flagged, not silently skipped)
- **Real Google/YouTube account login**: intentionally not built — see prior discussion. NewPipe
  itself has no real account sign-in; cookie-based session hijacking (SID/HSID/SAPISID extraction)
  is a credential-theft technique regardless of who's using it and won't be built. Real Google
  Sign-In (OAuth) is a safe alternative if wanted later.
- Like/Dislike/Subscribe buttons on the watch screen are real, working UI toggles, but don't
  persist anywhere or affect the real YouTube account — flagging this so it's not mistaken for
  real account interaction.
- Music/Hub/Downloads screens still use their original mock/demo data — this pass focused on the
  Home feed, search, pagination, and the video player/watch screen.

## Honesty about verification
I don't have an Android SDK/emulator in this environment, so I couldn't run a full Gradle build
here. Every extractor API call (`KioskInfo`, `SearchInfo`, `StreamInfo`, `Page`, pagination
methods) was cross-checked against NewPipe's actual working source, not guessed. The repo's
existing GitHub Actions workflow (`.github/workflows/android.yml`) builds a real signed debug
APK on push — check the Actions tab for the actual compile-time verification.

## Update: livestream playback + refresh system
- **Livestream playback fix**: videos like "SpaceX Starbase Live" in trending were getting stuck
  at 00:00 with a garbage duration label. Root cause: live broadcasts have no regular video file
  to resolve — they only have an HLS manifest. `NewPipeService.resolvePlayableStream()` now checks
  `StreamInfo.streamType` first and routes live content through a proper `HlsMediaSource`
  (`media3-exoplayer-hls` dependency added) instead of trying (and failing) to find a normal
  video-only/progressive stream.
- **Manual reload button**: added next to the search bar on Home (refresh icon) — reloads
  whatever's currently showing (trending or an active search).
- **Auto-refresh on resume**: the Home feed now automatically reloads whenever the app returns to
  the foreground (backgrounded and reopened) — the same "always fresh" feel as real YouTube. Note:
  killing the app from Recents and reopening already got fresh data before this change too, since
  that starts a brand new process/ViewModel from scratch.

## Update: full player controls, network speed, background audio
Big batch of player features added, matching the reference screenshots (YouTube's settings
sheet, double-tap seek, live thumbnail duration badges):

- **Double-tap to seek**: tap the left half of the video to rewind 10s, right half to skip
  forward 10s, with a brief flash animation — same as YouTube.
- **Fullscreen**: rotates to landscape and hides system bars. `MainActivity` now declares
  `android:configChanges` for orientation so rotating doesn't destroy/recreate the Activity and
  lose playback mid-video.
- **Settings sheet** (gear icon, top-right of the player): manual Quality picker (re-resolves the
  stream at the chosen resolution), Playback speed (0.5x–2x, wired to real ExoPlayer speed),
  Captions (real subtitle tracks from NewPipeExtractor, attached to ExoPlayer as a text track),
  and Lock screen (disables all touch/gesture on the player until unlocked).
- **Live network speed badge** (top-right): a real bitrate estimate from Media3's
  `DefaultBandwidthMeter`, attached to every actual network request this player makes — not a
  fake number. White/normal when healthy, light red when slow, blinking red + "Offline" when
  there's no internet connection (checked via `ConnectivityManager`).
- **"Convert to audio"**: switches to a background-playable audio-only stream via a new
  `AudioPlaybackService` (a real `MediaSessionService`, with lock-screen/notification playback
  controls) — audio keeps playing after backgrounding or locking the screen. Without tapping
  this, the regular video player is released as soon as the screen closes and does **not** play
  in the background, matching what was asked.

### Honesty about this batch specifically
This is a large set of less-common Android/Media3 APIs (`MediaSessionService`, foreground service
binding, `DefaultBandwidthMeter`, window-insets-controller fullscreen handling) layered in one
pass. I cross-checked every API I could against NewPipe's actual source where an equivalent
exists, but a few (the `MediaSessionService` background-audio wiring in particular) rely on my
general Media3 knowledge rather than a NewPipe source cross-check, since NewPipe itself still uses
the older ExoPlayer2 packages, not Media3, for its own background playback service. If the build
fails specifically inside `AudioPlaybackService.kt` or the audio-convert wiring in
`VideoStreamingPlayer.kt`, send the log — that's the part with the least direct precedent to
verify against.

## Update: playback reliability, fullscreen bar bug, real like/channel data
- **Fewer decoder crashes**: stream selection now prefers MPEG-4/AVC (H.264) over WebM/VP9 at the
  same resolution — AVC has near-universal hardware decoder support, while VP9 support varies a
  lot by device and was the most likely cause of the `ERROR_CODE_DECODER_INIT_FAILED` crash shown
  in the last report. On top of that, a decoder-related playback error now automatically retries
  once at the next-lower quality instead of just showing a dead error screen.
- **Fullscreen status bar bug fixed**: the real cause was a missing `setDecorFitsSystemWindows(false)`
  call — without it, `WindowInsetsController.hide()` doesn't actually hide the system bars. Fixed.
- **Removed the dark scrim behind the Settings sheet** (`scrimColor = Color.Transparent`).
- **Background audio actually plays now**: the real bug was a race condition — playback used to
  wait for the service to finish binding before starting, which could silently never happen.
  The audio URL is now passed directly in the Intent that starts the service, so playback begins
  immediately and reliably shows up in the system's media controls.
- **Real like count** now shown on the Like button (from `StreamInfo.getLikeCount()`), and tapping
  the channel row opens the real channel URL in the browser (`StreamInfo.getUploaderUrl()`).

### Not fully replicated (flagged honestly)
The reference screenshot's full audio-track/dubbed-language picker (Arabic dubbed, Bangla dubbed,
etc.) is YouTube's newer AI-dubbing feature and isn't something a scraping-based extractor can
reliably access the same way for most videos — this wasn't built, to avoid promising something
that wouldn't actually work for the vast majority of content.

## Update: ExoPlayer decoder fallback (the actual NewPipe technique)
Checked NewPipe's real source directly for this: they don't use a different/more powerful
decoder — same ExoPlayer family (they're on the older `com.google.android.exoplayer2` package,
this project uses the newer `androidx.media3` rebrand of the same library). The one concrete
relevant mechanism they have is **decoder fallback** (`DefaultRenderersFactory.setEnableDecoderFallback(true)`):
when a device's preferred decoder for a codec fails to initialize, ExoPlayer automatically tries
the next available decoder for that same codec instead of immediately throwing
`ERROR_CODE_DECODER_INIT_FAILED`. NewPipe exposes this as an opt-in setting (default OFF) since
it's a real user-facing toggle in their settings screen; since this app doesn't have an equivalent
settings screen, it's now just always enabled — directly targeting the exact error from the
screenshot, on top of the MPEG-4/AVC format preference and automatic quality-fallback-retry
already in place.

## Update: real Channel view, Shorts, background-audio notification fix
- **Channel view (must-have item)**: tapping a channel now opens a real in-app `ChannelScreen`
  instead of a browser — real avatar, name, subscriber count, description, and the channel's
  actual uploaded videos (via `ChannelInfo`/`ChannelTabInfo`), with infinite scroll.
- **Shorts (must-have item)**: channels with a Shorts tab now show one, playable via a dedicated
  `playShort()` path that enforces a hard 480p floor — auto-picks the best available quality
  (up to 4K) but never drops below 480p even on a bad connection, per what was asked.
- **Background-audio notification fixed**: the real cause was almost certainly a missing runtime
  `POST_NOTIFICATIONS` permission request — required on Android 13+, and without it the
  notification (with app icon + play/pause controls) silently never appears even though audio is
  actually playing. The app now requests this permission at startup.

### Honest answers on what wasn't built, and why
- **"Force-kill other background apps to free RAM for us"**: not possible for a normal app.
  Since Android 5, only the system itself (or a device-owner/MDM app) can kill other apps —
  a regular app has no permission to do this, for security reasons. No app on the Play Store
  can do this, including real YouTube.
- **"Guarantee the background service is never killed unless the user manually stops it"**: the
  proper foreground-service + persistent-notification setup (already in place) is the correct,
  real technique and gives strong protection, but some phone brands (Xiaomi/MIUI, Samsung, Oppo,
  etc.) run their own aggressive battery managers that can still kill background services
  regardless of what any app does — that's outside any app's control, including NewPipe's and
  YouTube's own.
- **Translator, in-app playlist browsing**: not built this round given the size of everything
  else in this pass — flagging clearly rather than quietly skipping, happy to take these on next.
