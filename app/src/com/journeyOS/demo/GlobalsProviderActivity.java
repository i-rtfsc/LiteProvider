package com.journeyOS.demo;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.journeyOS.liteprovider.globals.GlobalsManager;


public class GlobalsProviderActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {
    private static final String TAG = "GlobalsProvider";
    private Context mContext;
    private SharedPreferences mPreferences;

    private EditText mKey;
    private EditText mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_provider_activity);
        mContext = getApplicationContext();

        mKey = (EditText) findViewById(R.id.key);

        mValue = (EditText) findViewById(R.id.value);

        findViewById(R.id.insert).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);

        mPreferences = GlobalsManager.getSharedPreferences();

        new GlobalsKeyObserver(new Handler());

        GlobalsManager.registerOnGlobalsProviderChangeListener(new GlobalsManager.OnGlobalsProviderChangeListener() {
            @Override
            public void onGlobalsProviderChanged(String key) {
                Log.d(TAG, "on globals provider changed, key = [" + key + "], value = [" + GlobalsManager.getString("globals_key") + "]");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "on shared preferences changed, key = [" + key + "], value = [" + GlobalsManager.getString("globals_key") + "]");
    }

    @Override
    public void onClick(View view) {
        String key = mKey.getText().toString();
        String value = mValue.getText().toString();
        switch (view.getId()) {
            case R.id.insert:
                GlobalsManager.put(key, value);
                break;
            case R.id.update:
                GlobalsManager.put(key, value);
                break;
            case R.id.delete:
                GlobalsManager.remove(key);
                break;
            default:
                break;
        }
    }


    private final class GlobalsKeyObserver extends ContentObserver {
        private final Uri mUri = GlobalsManager.getUriFor("globals_key");

        public GlobalsKeyObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = mContext.getContentResolver();
            Log.d(TAG, "Uri = [" + mUri + "]");
            resolver.registerContentObserver(mUri, false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "on changed, value = [" + GlobalsManager.getString("globals_key") + "]");
        }
    }
}
