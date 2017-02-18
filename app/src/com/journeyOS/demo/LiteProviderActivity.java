package com.journeyOS.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.Map;

public class LiteProviderActivity extends Activity implements View.OnClickListener {
    private static final String TAG = LiteProviderActivity.class.getSimpleName();

    private Context mContext;

    private EditText mKey;
    private EditText mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lite_provider_activity);

        mContext = getApplicationContext();

        mKey = (EditText) findViewById(R.id.key);

        mValue = (EditText) findViewById(R.id.value);

        findViewById(R.id.insert).setOnClickListener(this);
        findViewById(R.id.query).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        String key = mKey.getText().toString();
        String value = mValue.getText().toString();
        switch (view.getId()) {
            case R.id.insert:
                DBOperate.getDefault().saveOrUpdate(mContext, key, value);
                break;
            case R.id.query:
                Map<String, String> result = DBOperate.getDefault().query(mContext, key);
                for (Map.Entry<String, String> map : result.entrySet()) {
                    Log.d(TAG, "query result , key = [" + map.getKey() + "],  value = [" + map.getValue() + "]");
                }
                break;
            default:
                break;
        }
    }
}
