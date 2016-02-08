package io.github.epelde.didactictribble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

/**
 * Created by epelde on 08/02/2016.
 */
public class ConfigActivity extends Activity {

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private static final int REQUEST_CODE_ENABLE_BT = 2;


    private TextView message;
    private Button selectBtn;
    BluetoothService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        message = (TextView) findViewById(R.id.message_text_view);
        selectBtn = (Button) findViewById(R.id.select_button);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service.isAvailable()) {
                    if(!service.isBTopen()) {
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                REQUEST_CODE_ENABLE_BT);
                    } else {
                        startActivityForResult(new Intent(ConfigActivity.this, DeviceListActivity.class),
                                REQUEST_CODE_SELECT_DEVICE);
                    }
                }
            }
        });
        String printerAddress = getPreferences(this.MODE_PRIVATE)
                .getString(getString(R.string.KEY_PRINTER_ADRESS), null);
        if (printerAddress != null) {
        } else {
            message.setText(getString(R.string.msg_no_printer));
        }
        service = new BluetoothService(this, handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.msg_bluetooth_enabled, Toast.LENGTH_LONG).show();
        }
        if (requestCode == REQUEST_CODE_SELECT_DEVICE && resultCode == RESULT_OK) {

        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothService.STATE_CONNECTED:
                        Toast.makeText(getApplicationContext(), R.string.msg_connected,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:
                Toast.makeText(getApplicationContext(), R.string.msg_connection_lost,
                        Toast.LENGTH_SHORT).show();
                break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:
                Toast.makeText(getApplicationContext(), R.string.msg_unable_connect,
                        Toast.LENGTH_SHORT).show();
                break;
        }
        }
    };
}
