package com.venomdino.exonetworkstreamer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.venomdino.exonetworkstreamer.R;
import com.venomdino.exonetworkstreamer.adapters.CustomSpinnerAdapter;
import com.venomdino.exonetworkstreamer.helpers.CustomMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UnstableApi public class MainActivity extends AppCompatActivity {

    private Spinner userAgentSpinner, drmSchemeSelector;
    private TextInputLayout mediaUrlTil, drmLicenceTil;
    private TextInputEditText mediaUrlTiet, drmLicenceTiet;
    private MaterialButton playBtn;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout formContainer;
    private static final int RC_APP_UPDATE = 12345;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

//        ------------------------------------------------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        toggle.syncState();

        drawerLayout.addDrawerListener(toggle);

        View headView = navigationView.getHeaderView(0);

        ((TextView) headView.findViewById(R.id.header_layout_version_tv)).setText("Version: " + CustomMethods.getVersionName(this));

        navigationViewItemClickedActions(navigationView);
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
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        ------------------------------------------------------------------------------------------

        formContainer.setOnClickListener(view -> {

            List<TextInputEditText> textInputEditTextList = findAllTextInputEditText();

            for (TextInputEditText editText : textInputEditTextList) {
                editText.clearFocus();
                CustomMethods.hideSoftKeyboard(MainActivity.this, editText);
            }
        });

//        ------------------------------------------------------------------------------------------

        playBtn.setOnClickListener(view -> {

            boolean shouldStartPlaying = true;

            String mediaStreamUrl = Objects.requireNonNull(mediaUrlTiet.getText()).toString().trim();
            String drmLicenceUrl = Objects.requireNonNull(drmLicenceTiet.getText()).toString().trim();

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

                if(!CustomMethods.isValidURL(drmLicenceUrl)){
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

//        ------------------------------------------------------------------------------------------

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            RC_APP_UPDATE
                    );
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(this, "New update available but failed to show update dialog.", Toast.LENGTH_SHORT).show();
                }
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

        drawerLayout = findViewById(R.id.root_layout);
        navigationView = findViewById(R.id.navigation_drawer);

        formContainer = findViewById(R.id.formContainer);
    }
//    ==============================================================================================

    private List<TextInputEditText> findAllTextInputEditText() {

        List<TextInputEditText> editTextList = new ArrayList<>();

        editTextList.add(mediaUrlTiet);
        editTextList.add(drmLicenceTiet);

        return editTextList;
    }

//    ==============================================================================================

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "You are up-to-date.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please update app", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "An error occurred during update.", Toast.LENGTH_SHORT).show();
            }
        }
    }
//    ==============================================================================================
    private void navigationViewItemClickedActions(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.report_bug_action){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_repo_link))));
            }
            else if (item.getItemId() == R.id.share_action){
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_sharing_message) + getPackageName());
                startActivity(Intent.createChooser(intent1, "Share via"));
            }

            else if (item.getItemId() == R.id.more_apps_action) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_apps))));
            }
            else if (item.getItemId() == R.id.visit_telegram){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_channel))));
            }
            return false;
        });
    }
}