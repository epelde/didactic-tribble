package io.github.epelde.didactictribble;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by epelde on 08/02/2016.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button configBtn = (Button) findViewById(R.id.config_button);
        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(v.getContext(), DeviceListActivity.class),
                        REQUEST_CODE_SELECT_DEVICE);
            }
        });
        Button printBtn = (Button) findViewById(R.id.print_button);
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), PrintTicketActivity.class));
            }
        });
        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BarcodeScanner.class));
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
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putString(getString(R.string.KEY_PRINTER_ADRESS), address);
                    editor.commit();
                    Toast.makeText(MainActivity.this, R.string.toast_msg_printer_configured, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
