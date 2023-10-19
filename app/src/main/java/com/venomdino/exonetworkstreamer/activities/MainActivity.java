package com.venomdino.exonetworkstreamer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.media3.common.util.UnstableApi;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.venomdino.exonetworkstreamer.BuildConfig;
import com.venomdino.exonetworkstreamer.R;
import com.venomdino.exonetworkstreamer.adapters.CustomSpinnerAdapter;
import com.venomdino.exonetworkstreamer.databinding.ActivityMainBinding;
import com.venomdino.exonetworkstreamer.helpers.CustomMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int RC_APP_UPDATE = 12345;
    private String userAgent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ------------------------------------------------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.getRoot(), toolbar, 0, 0);
        toggle.syncState();

        binding.getRoot().addDrawerListener(toggle);

        View headView = binding.navigationView.getHeaderView(0);

        ((TextView) headView.findViewById(R.id.header_layout_version_tv)).setText("Version: " + CustomMethods.getVersionName(this));

        navigationViewItemClickedActions(binding.navigationView);

//        ------------------------------------------------------------------------------------------


        String[] userAgentBrowserNames = getResources().getStringArray(R.array.agent_browsers_names);
        String userAgentPlaceholder = "User-agent (Default)";

        CustomSpinnerAdapter userAgentAdapter = new CustomSpinnerAdapter(this, userAgentBrowserNames, userAgentPlaceholder);

        binding.userAgentSpinner.setAdapter(userAgentAdapter);

        binding.userAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                userAgentAdapter.setShowPlaceholder(false);
                // Handle the selected item

                if (position == 1) {
                    userAgent = getString(R.string.chrome_android_agent);
                } else if (position == 2) {
                    userAgent = getString(R.string.firefox_android_agent);
                } else if (position == 3) {
                    userAgent = getString(R.string.chrome_windows_agent);
                } else if (position == 4) {
                    userAgent = getString(R.string.firefox_windows_agent);
                } else if (position == 5) {

                    float density = getResources().getDisplayMetrics().density;
                    int marginHorizontal = (int) (20 * density);

                    FrameLayout container = new FrameLayout(MainActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.leftMargin = marginHorizontal;
                    params.rightMargin = marginHorizontal;

                    final EditText editText = new EditText(MainActivity.this);
                    editText.setHint("Enter custom user-agent");
                    editText.setLayoutParams(params);
                    container.addView(editText);


                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setCancelable(false);
                    alert.setTitle("Custom User-Agent");
                    alert.setView(container);

                    alert.setPositiveButton("OK", (dialog, whichButton) -> {

                        String customUserAgent = editText.getText().toString();

                        if (customUserAgent.equals("")) {
                            userAgent = getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME + " (Linux; Android " + Build.VERSION.RELEASE + ")";
                            binding.userAgentSpinner.setSelection(0);
                        } else {
                            userAgent = customUserAgent;
                        }

                        dialog.dismiss();
                    });

                    alert.show();

                } else {
                    userAgent = getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME + " (Linux; Android " + Build.VERSION.RELEASE + ")";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        ------------------------------------------------------------------------------------------

        String[] drmSchemes = getResources().getStringArray(R.array.drm_schemes);

        String drmSchemePlaceholder = "DrmScheme (Widevine)";

        CustomSpinnerAdapter drmSchemeAdapter = new CustomSpinnerAdapter(this, drmSchemes, drmSchemePlaceholder);

        binding.drmSchemeSelector.setAdapter(drmSchemeAdapter);

        binding.drmSchemeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                drmSchemeAdapter.setShowPlaceholder(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        ------------------------------------------------------------------------------------------

        binding.playBtn.setOnClickListener(view -> {

            boolean shouldStartPlaying = true;

            String mediaStreamUrl = Objects.requireNonNull(binding.mediaStreamUrlEt.getText()).toString().trim();
            String drmLicenceUrl = Objects.requireNonNull(binding.drmLicenceUrlEt.getText()).toString().trim();
            String refererValue = Objects.requireNonNull(binding.refererEt.getText()).toString();


            int selectedDrmScheme = binding.drmSchemeSelector.getSelectedItemPosition();

            if (mediaStreamUrl.equalsIgnoreCase("")) {
                binding.mediaStreamUrlTil.setErrorEnabled(true);
                binding.mediaStreamUrlTil.setError("Media stream link required.");
                shouldStartPlaying = false;
            } else if (!CustomMethods.isValidURL(mediaStreamUrl)) {
                binding.mediaStreamUrlTil.setErrorEnabled(true);
                binding.mediaStreamUrlTil.setError("Invalid Link.");
                shouldStartPlaying = false;
            }

            if (!drmLicenceUrl.equalsIgnoreCase("")) {

                if (!CustomMethods.isValidURL(drmLicenceUrl)) {
                    binding.drmLicenceUrlTil.setErrorEnabled(true);
                    binding.drmLicenceUrlTil.setError("Invalid Link.");
                    shouldStartPlaying = false;
                }
            }

            if (shouldStartPlaying) {

                binding.mediaStreamUrlTil.setErrorEnabled(false);
                binding.drmLicenceUrlTil.setErrorEnabled(false);

                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("mediaStreamUrl", mediaStreamUrl);
                intent.putExtra("drmLicenceUrl", drmLicenceUrl);
                intent.putExtra("refererValue", refererValue);
                intent.putExtra("userAgent", userAgent);
                intent.putExtra("selectedDrmScheme", selectedDrmScheme);
                startActivity(intent);
            }
        });

//        ------------------------------------------------------------------------------------------

        binding.formContainer.setOnClickListener(view -> {

            List<TextInputEditText> textInputEditTextList = findAllTextInputEditText();

            for (TextInputEditText editText : textInputEditTextList) {
                editText.clearFocus();
                CustomMethods.hideSoftKeyboard(MainActivity.this, editText);
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

    private List<TextInputEditText> findAllTextInputEditText() {

        List<TextInputEditText> editTextList = new ArrayList<>();

        editTextList.add(binding.mediaStreamUrlEt);
        editTextList.add(binding.drmLicenceUrlEt);
        editTextList.add(binding.refererEt);

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

            if (item.getItemId() == R.id.report_bug_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_repo_link))));

            } else if (item.getItemId() == R.id.rate_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

            } else if (item.getItemId() == R.id.share_action) {

                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_sharing_message) + getPackageName());
                startActivity(Intent.createChooser(intent1, "Share via"));

            } else if (item.getItemId() == R.id.more_apps_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_apps))));

            } else if (item.getItemId() == R.id.visit_telegram) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_channel))));
            }
            return false;
        });
    }
}