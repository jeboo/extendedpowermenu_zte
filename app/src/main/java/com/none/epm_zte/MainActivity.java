package com.none.epm_zte;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = getSharedPreferences("epm_prefs", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        final CheckBox cbBL = (CheckBox) findViewById(R.id.addBL);
        cbBL.setChecked(sharedPreferences.getBoolean("addBL", false));
        cbBL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("addBL", cbBL.isChecked()).commit();
            }
        });

        findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
