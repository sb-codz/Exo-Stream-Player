package com.venomdino.exonetworkstreamer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.ui.PlayerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venomdino.exonetworkstreamer.R;
import com.venomdino.exonetworkstreamer.helpers.CustomMethods;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.UUID;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {
    String mediaStreamUrl, drmLicenceUrl;
    String userAgent;
    int selectedDrmScheme, selectedUserAgent;
    PlayerView playerView;
    ProgressBar bufferProgressbar;
    ExoPlayer exoPlayer;
    DefaultTrackSelector defaultTrackSelector;
    ArrayList<String> videoQualities;
    TextView fileNameTV;
    ImageButton qualitySelectionBtn;
    int selectedQualityIndex = 0;
    UUID drmScheme;
    private boolean playWhenReady = true;
    private long playbackPosition = C.TIME_UNSET;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_player);
        CustomMethods.hideSystemUI(this);

        Intent intent = getIntent();

        mediaStreamUrl = intent.getStringExtra("mediaStreamUrl");
        drmLicenceUrl = intent.getStringExtra("drmLicenceUrl");
        selectedUserAgent = intent.getIntExtra("selectedAgent", 0);
        selectedDrmScheme = intent.getIntExtra("selectedDrmScheme", 0);


        if (drmLicenceUrl.equalsIgnoreCase("none")) {
            drmLicenceUrl = getString(R.string.default_drm_licence_url);
        }

        if (selectedUserAgent == 1) {
            userAgent = getString(R.string.chrome_android_agent);
        } else if (selectedUserAgent == 2) {
            userAgent = getString(R.string.chrome_windows_agent);
        } else if (selectedUserAgent == 3) {
            userAgent = getString(R.string.firefox_android_agent);
        } else if (selectedUserAgent == 4) {
            userAgent = getString(R.string.firefox_windows_agent);
        } else {
            userAgent = getString(R.string.default_useragent);
        }

        if (selectedDrmScheme == 0) {
            drmScheme = C.WIDEVINE_UUID;
        } else if (selectedDrmScheme == 1) {
            drmScheme = C.PLAYREADY_UUID;
        } else if (selectedDrmScheme == 2) {
            drmScheme = C.CLEARKEY_UUID;
        } else {
            drmScheme = C.WIDEVINE_UUID;
        }

        initVars();

        initializePlayer();
    }

    //______________________________________________________________________________________________

    private void initializePlayer() {

        @SuppressLint("UnsafeOptInUsageError")
        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();

        defaultTrackSelector = new DefaultTrackSelector(this, videoTrackSelectionFactory);
        defaultTrackSelector.setParameters(defaultTrackSelector.getParameters().buildUpon()
                .setPreferredTextLanguage("en")
                .build());

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .forceEnableMediaCodecAsynchronousQueueing()
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                .build();

        //*********************************************************************************

        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setSeekForwardIncrementMs(10000)
                .setSeekBackIncrementMs(10000)
                .setTrackSelector(defaultTrackSelector)
                .setLoadControl(loadControl)
                .build();

        if (mediaStreamUrl.toLowerCase().contains(".m3u8")) {
            MediaSource mediaSource = buildHlsMediaSource(Uri.parse(mediaStreamUrl), userAgent, drmLicenceUrl);
            exoPlayer.setMediaSource(mediaSource);
        } else if (mediaStreamUrl.toLowerCase().contains(".mpd")) {
            MediaSource mediaSource = buildDashMediaSource(Uri.parse(mediaStreamUrl), userAgent, drmLicenceUrl);
            exoPlayer.setMediaSource(mediaSource);
        } else {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(mediaStreamUrl)));
        }

        exoPlayer.prepare();

        exoPlayer.setPlayWhenReady(playWhenReady);

        if (playbackPosition != C.TIME_UNSET) {
            exoPlayer.seekTo(playbackPosition);
        }
        exoPlayer.addListener(new Player.Listener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onPlaybackStateChanged(int playbackState) {

                if (playbackState == Player.STATE_BUFFERING) {
                    bufferProgressbar.setVisibility(View.VISIBLE);
                }
                if (playbackState == Player.STATE_READY) {

                    playerView.setVisibility(View.VISIBLE);

                    bufferProgressbar.setVisibility(View.GONE);

                    fileNameTV.setText(CustomMethods.getFileName(mediaStreamUrl));

                    videoQualities = getVideoQualitiesTracks();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {

                playerView.setVisibility(View.GONE);
                bufferProgressbar.setVisibility(View.GONE);

                exoPlayer.stop();
                exoPlayer.release();

                CustomMethods.errorAlert(PlayerActivity.this, "Error", error.getMessage(), "Ok", true);
            }
        });

        playerView.setPlayer(exoPlayer);
        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setControllerShowTimeoutMs(2500);

//        ..........................................................................................

        qualitySelectionBtn.setOnClickListener(view -> {

            if (videoQualities != null) {

                if (videoQualities.size() > 0) {
                    getQualityChooserDialog(this, videoQualities);
                } else {
                    Toast.makeText(this, "No video quality found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Wait until video start.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //______________________________________________________________________________________________

    private DefaultDrmSessionManager buildDrmSessionManager(UUID uuid, String userAgent, String drmLicenseUrl) {

        HttpDataSource.Factory licenseDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(userAgent);

        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(drmLicenseUrl, true,
                licenseDataSourceFactory);

        return new DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(uuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(drmCallback);
    }

    //    ----------------------------------------------------------------------------------------------
    private DashMediaSource buildDashMediaSource(Uri uri, String userAgent, String drmLicenceUrl) {

        UUID drmSchemeUuid = Util.getDrmUuid(drmScheme.toString());

        DrmSessionManager drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenceUrl, userAgent);

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, new DefaultHttpDataSource.Factory().setUserAgent(userAgent));

        return new DashMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                .createMediaSource(
                        new MediaItem.Builder()
                                .setUri(uri)
                                .setMimeType(MimeTypes.APPLICATION_MPD)
                                .build()
                );
    }

    //    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private HlsMediaSource buildHlsMediaSource(Uri uri, String userAgent, String drmLicenceUrl) {

        UUID drmSchemeUuid = Util.getDrmUuid(drmScheme.toString());

        DrmSessionManager drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenceUrl, userAgent);

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, new DefaultHttpDataSource.Factory().setUserAgent(userAgent));

        return new HlsMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                .createMediaSource(
                        new MediaItem.Builder()
                                .setUri(uri)
                                .setMimeType(MimeTypes.APPLICATION_M3U8)
                                .build()
                );
    }

    //______________________________________________________________________________________________

    private ArrayList<String> getVideoQualitiesTracks() {

        ArrayList<String> videoQualities = new ArrayList<>();

        MappingTrackSelector.MappedTrackInfo renderTrack = defaultTrackSelector.getCurrentMappedTrackInfo();
        assert renderTrack != null;
        int renderCount = renderTrack.getRendererCount();

        for (int rendererIndex = 0; rendererIndex < renderCount; rendererIndex++) {

            if (isSupportedFormat(renderTrack, rendererIndex)) {

                int trackGroupType = renderTrack.getRendererType(rendererIndex);
                TrackGroupArray trackGroups = renderTrack.getTrackGroups(rendererIndex);
                int trackGroupsCount = trackGroups.length;

                if (trackGroupType == C.TRACK_TYPE_VIDEO) {

                    for (int groupIndex = 0; groupIndex < trackGroupsCount; groupIndex++) {

                        int videoQualityTrackCount = trackGroups.get(groupIndex).length;

                        for (int trackIndex = 0; trackIndex < videoQualityTrackCount; trackIndex++) {

                            boolean isTrackSupported = renderTrack.getTrackSupport(rendererIndex, groupIndex, trackIndex) == C.FORMAT_HANDLED;

                            if (isTrackSupported) {

                                TrackGroup track = trackGroups.get(groupIndex);

                                int videoWidth = track.getFormat(trackIndex).width;
                                int videoHeight = track.getFormat(trackIndex).height;

                                String quality = videoWidth + "x" + videoHeight;
                                videoQualities.add(quality);
                            }
                        }
                    }
                }
            }
        }

        return videoQualities;
    }

    private boolean isSupportedFormat(MappingTrackSelector.MappedTrackInfo mappedTrackInfo, int rendererIndex) {

        TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex);
        if (trackGroupArray.length == 0) {
            return false;
        } else {
            return mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO;
        }
    }
    //______________________________________________________________________________________________

    private void getQualityChooserDialog(Context context, ArrayList<String> arrayList) {

        CharSequence[] charSequences = new CharSequence[arrayList.size() + 1];
        charSequences[0] = "Auto";

        for (int i = 0; i < arrayList.size(); i++) {
            charSequences[i + 1] = arrayList.get(i); //.split("x")[1] + "p";
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Select video quality:");
        builder.setSingleChoiceItems(charSequences, selectedQualityIndex, (dialogInterface, which) -> selectedQualityIndex = which);
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {

            if (selectedQualityIndex == 0) {
                Toast.makeText(context, context.getText(R.string.app_name) + " will choose video resolution automatically.", Toast.LENGTH_SHORT).show();
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters().setMaxVideoSizeSd());
            } else {
                String[] videoQualityInfo = arrayList.get(selectedQualityIndex - 1).split("x");

                Toast.makeText(context, "Video will be played with " + videoQualityInfo[1] + "p resolution.", Toast.LENGTH_SHORT).show();

                int videoWidth = Integer.parseInt(videoQualityInfo[0]);
                int videoHeight = Integer.parseInt(videoQualityInfo[1]);

                defaultTrackSelector.setParameters(
                        defaultTrackSelector
                                .buildUponParameters()
                                .setMaxVideoSize(videoWidth, videoHeight)
                                .setMinVideoSize(videoWidth, videoHeight)
                );
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }

    //______________________________________________________________________________________________

    /**
     Save and Restore Playback State:
     To maintain the playback state across different app states (minimized, restored),
     you can save and restore the playback state. You can do this using the
     onSaveInstanceState and onRestoreInstanceState methods. Here's an example:
     */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("playWhenReady", exoPlayer.getPlayWhenReady());
        outState.putLong("playbackPosition", exoPlayer.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        playWhenReady = savedInstanceState.getBoolean("playWhenReady");
        playbackPosition = savedInstanceState.getLong("playbackPosition", C.TIME_UNSET);
    }
    //______________________________________________________________________________________________

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    //______________________________________________________________________________________________
    private void initVars() {
        playerView = findViewById(R.id.exo_player_view);
        bufferProgressbar = findViewById(R.id.buffer_progressbar);
        fileNameTV = findViewById(R.id.file_name_tv);
        qualitySelectionBtn = findViewById(R.id.quality_selection_btn);
    }
}