package com.venomdino.exonetworkstreamer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.util.UnstableApi;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.venomdino.exonetworkstreamer.R;
import com.venomdino.exonetworkstreamer.adapters.FragmentAdapter;
import com.venomdino.exonetworkstreamer.databinding.ActivityMainBinding;
import com.venomdino.exonetworkstreamer.helpers.CustomMethods;

import java.util.Objects;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private static final int RC_APP_UPDATE = 12345;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentAdapter adapter = new FragmentAdapter(fragmentManager, getLifecycle(), binding.bottomNavView.getMaxItemCount());
        binding.viewPager2.setAdapter(adapter);
        binding.viewPager2.setUserInputEnabled(false);

        binding.bottomNavView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_local_btn){
                binding.viewPager2.setCurrentItem(2);
            } else {
                binding.viewPager2.setCurrentItem(1);
            }

            return true;
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