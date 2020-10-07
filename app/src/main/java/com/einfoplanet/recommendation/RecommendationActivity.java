package com.einfoplanet.recommendation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.tvprovider.media.tv.TvContractCompat;

import com.einfoplanet.recommendation.recommendation.MockDataService;
import com.einfoplanet.recommendation.recommendation.RecommendationUtil;

public class RecommendationActivity extends Activity {
    private static final String TAG = RecommendationActivity.class.getSimpleName();
    private static final int MAKE_BROWSABLE_REQUEST_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        findViewById(R.id.btn_subscription).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    long channelId = RecommendationUtil.createChannel(RecommendationActivity.this, "NEW2020");
//                    promptUserToDisplayChannel(channelId);
                    TvContractCompat.requestChannelBrowsable(RecommendationActivity.this, channelId);
                    RecommendationUtil.createPrograms(RecommendationActivity.this, channelId, MockDataService.getList());
                }
            }
        });
    }

    private void promptUserToDisplayChannel(long channelId) {
        Intent intent = new Intent(TvContractCompat.ACTION_REQUEST_CHANNEL_BROWSABLE);
        intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId);
        try {
            this.startActivityForResult(intent, MAKE_BROWSABLE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Could not start activity: " + intent.getAction(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Channel Added", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Channel Not Added", Toast.LENGTH_LONG).show();
        }
    }
}