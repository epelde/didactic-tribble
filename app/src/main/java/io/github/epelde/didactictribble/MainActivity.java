package io.github.epelde.didactictribble;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by epelde on 08/02/2016.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Button configBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configBtn = (Button) findViewById(R.id.config_button);
        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class),
                        REQUEST_CODE_SELECT_DEVICE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.i(LOG_TAG, "* * * * * * * * * * * * * * * *");
                    Log.i(LOG_TAG, "* * * ADDRESS:" + address);
                    Log.i(LOG_TAG, "* * * * * * * * * * * * * * * *");
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putString(getString(R.string.KEY_PRINTER_ADRESS), address);
                    editor.commit();
                }
                break;
        }
    }
}
