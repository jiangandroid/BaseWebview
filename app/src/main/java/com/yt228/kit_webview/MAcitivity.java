package com.yt228.kit_webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jiang.baseWebview.YTJavaScriptFactory;
import com.jiang.baseWebview.YtJsRegistor;

import java.util.Map;

/**
 * Created by xiyou on 2019/4/8
 */
public class MAcitivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        findViewById(R.id.btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, String> map = YtJsRegistor.getHandlerRegistryMap();
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            Log.e("YtJsRegistor", entry.getKey() + ":" + entry.getValue());
                        }
                        YTJavaScriptFactory.handleFunction(MAcitivity.this, "share", "", "");
                    }
                });
    }
}
