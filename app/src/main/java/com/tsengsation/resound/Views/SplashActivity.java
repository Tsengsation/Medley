package com.tsengsation.resound.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.tsengsation.resound.Parse.ParseMedley;
import com.tsengsation.resound.Parse.ParseMedley.OnDownloadCompletedListener;
import com.tsengsation.resound.R;

/**
 * Initial landing page to show while downloading data in background.
 */
public class SplashActivity extends Activity implements OnDownloadCompletedListener {

    private ParseMedley mParseResound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        mParseResound = ParseMedley.getInstance();
        mParseResound.setOnDownloadCompleted(this);
        mParseResound.downloadData();
    }

    @Override
    public void onDownloadSuccess() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onDownloadFail() {
        // TODO dialog:
        Toast.makeText(getApplicationContext(), "Download failed.", Toast.LENGTH_LONG).show();
    }
}
