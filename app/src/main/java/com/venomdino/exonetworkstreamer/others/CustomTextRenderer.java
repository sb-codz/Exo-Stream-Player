package com.venomdino.exonetworkstreamer.others;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.media3.common.text.Cue;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.text.TextOutput;
import androidx.media3.exoplayer.text.TextRenderer;

import java.util.ArrayList;
import java.util.List;

@UnstableApi public class CustomTextRenderer implements TextRenderer {

    private static final String TAG = "CustomTextRenderer";

    private final Handler handler;
    private final TextOutput textOutput;
    private List<Cue> validCues;

    public CustomTextRenderer(TextOutput textOutput, Looper looper) {
        this.textOutput = Assertions.checkNotNull(textOutput);
        this.handler = new Handler(looper);
        this.validCues = new ArrayList<>();
    }

    @Override
    public void onCues(List<Cue> cues) {
        List<Cue> filteredCues = filterEmptyCues(cues);
        textOutput.onCues(filteredCues);
    }

    private List<Cue> filterEmptyCues(List<Cue> cues) {
        validCues.clear();

        for (Cue cue : cues) {
            if (!isEmptyCue(cue)) {
                validCues.add(cue);
            }
        }

        return validCues;
    }

    private boolean isEmptyCue(Cue cue) {
        return cue.text == null || cue.text.toString().trim().isEmpty();
    }

    @Override
    public void onInitializationSuccess(SimpleSubtitleDecoder decoder) {
        // Implementation not needed for this example
    }

    @Override
    public void onInitializationError(Exception e) {
        Log.e(TAG, "Subtitle decoder initialization error: " + e.getMessage());
        // Handle initialization error if needed
    }

    @Override
    public Subtitle createSubtitle() {
        return new SimpleSubtitle();
    }
}
