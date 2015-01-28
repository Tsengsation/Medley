package com.tsengsation.resound.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.tsengsation.resound.Events.OnDownloadCompletedListener;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.R;

/**
 * Created by jonathantseng on 1/28/15.
 */
public class SplashActivity extends Activity {

    private ParseResound mParseResound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        mParseResound = ParseResound.getInstance();
        mParseResound.setOnDownloadCompleted(new OnDownloadCompletedListener() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }

            @Override
            public void onFail() {
                // TODO dialog:
                Toast.makeText(getApplicationContext(), "download failed", Toast.LENGTH_LONG).show();
            }
        });
        mParseResound.downloadData();
    }
}
