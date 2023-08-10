package com.venomdino.exonetworkstreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.venomdino.exonetworkstreamer.R;
import com.venomdino.exonetworkstreamer.adapters.CustomSpinnerAdapter;
import com.venomdino.exonetworkstreamer.helpers.CustomMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UnstableApi public class MainActivity extends AppCompatActivity {

    Spinner userAgentSpinner, drmSchemeSelector;
    TextInputLayout mediaUrlTil, drmLicenceTil;
    TextInputEditText mediaUrlTiet, drmLicenceTiet;
    MaterialButton playBtn;
    LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

//        ------------------------------------------------------------------------------------------
        String[] userAgentBrowserNames = getResources().getStringArray(R.array.agent_browsers_names);
        String userAgentPlaceholder = "User-agent (Default)";

        CustomSpinnerAdapter userAgentAdapter = new CustomSpinnerAdapter(this, userAgentBrowserNames, userAgentPlaceholder);

        userAgentSpinner.setAdapter(userAgentAdapter);

        userAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                userAgentAdapter.setShowPlaceholder(false);
                // Handle the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        ------------------------------------------------------------------------------------------

        String[] drmSchemes = getResources().getStringArray(R.array.drm_schemes);

        String drmSchemePlaceholder = "DrmScheme (Widevine)";

        CustomSpinnerAdapter drmSchemeAdapter = new CustomSpinnerAdapter(this, drmSchemes, drmSchemePlaceholder);

        drmSchemeSelector.setAdapter(drmSchemeAdapter);

        drmSchemeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                drmSchemeAdapter.setShowPlaceholder(false);
                // Handle the selected item
                String selectedItem = drmSchemes[position];

                Toast.makeText(MainActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        ------------------------------------------------------------------------------------------

        rootLayout.setOnClickListener(view -> {

            List<TextInputEditText> textInputEditTextList = findAllTextInputEditText();

            for (TextInputEditText editText : textInputEditTextList) {
                editText.clearFocus();
                CustomMethods.hideSoftKeyboard(MainActivity.this);
            }
        });

//        ------------------------------------------------------------------------------------------

        playBtn.setOnClickListener(view -> {

            boolean shouldStartPlaying = true;

            String mediaStreamUrl = Objects.requireNonNull(mediaUrlTiet.getText()).toString();
            String drmLicenceUrl = Objects.requireNonNull(drmLicenceTiet.getText()).toString();

            int selectedAgent = userAgentSpinner.getSelectedItemPosition();
            int selectedDrmScheme = drmSchemeSelector.getSelectedItemPosition();

            if (mediaStreamUrl.equalsIgnoreCase("")){
                mediaUrlTil.setErrorEnabled(true);
                mediaUrlTil.setError("Media stream link required.");
                shouldStartPlaying = false;
            }
            else if(!CustomMethods.isValidURL(mediaStreamUrl)){
                mediaUrlTil.setErrorEnabled(true);
                mediaUrlTil.setError("Invalid Link.");
                shouldStartPlaying = false;
            }

            if (!drmLicenceUrl.equalsIgnoreCase("")){

                if(CustomMethods.isValidURL(drmLicenceUrl)){
                    drmLicenceTil.setErrorEnabled(true);
                    drmLicenceTil.setError("Invalid Link.");
                    shouldStartPlaying = false;
                }
            }
            else{
                drmLicenceUrl = "none";
            }


            if (shouldStartPlaying){

                mediaUrlTil.setErrorEnabled(false);
                drmLicenceTil.setErrorEnabled(false);

                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("mediaStreamUrl", mediaStreamUrl);
                intent.putExtra("drmLicenceUrl", drmLicenceUrl);
                intent.putExtra("selectedAgent", selectedAgent);
                intent.putExtra("selectedDrmScheme", selectedDrmScheme);
                startActivity(intent);
            }
        });
    }

//    ==============================================================================================
    private void initVars() {
        userAgentSpinner = findViewById(R.id.userAgentSpinner);
        drmSchemeSelector = findViewById(R.id.drmSchemeSelector);

        mediaUrlTil = findViewById(R.id.media_stream_url_til);
        drmLicenceTil = findViewById(R.id.drm_licence_url_til);

        mediaUrlTiet = findViewById(R.id.media_stream_url_et);
        drmLicenceTiet = findViewById(R.id.drm_licence_url_et);

        playBtn = findViewById(R.id.play_btn);

        rootLayout = findViewById(R.id.root_layout);
    }
//    ==============================================================================================

    private List<TextInputEditText> findAllTextInputEditText() {

        List<TextInputEditText> editTextList = new ArrayList<>();

        editTextList.add(mediaUrlTiet);
        editTextList.add(drmLicenceTiet);

        return editTextList;
    }
}