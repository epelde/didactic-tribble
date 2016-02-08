package io.github.epelde.didactictribble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by epelde on 08/02/2016.
 */
public class ConfigActivity extends Activity {

    private static final int SELECT_DEVICE_REQUEST_CODE = 1;

    private TextView message;
    private Button selectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        message = (TextView) findViewById(R.id.message_text_view);
        selectBtn = (Button) findViewById(R.id.select_button);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ConfigActivity.this, DeviceListActivity.class),
                        SELECT_DEVICE_REQUEST_CODE);
            }
        });
        String printerAddress = getPreferences(this.MODE_PRIVATE)
                .getString(getString(R.string.KEY_PRINTER_ADRESS), null);
        if (printerAddress != null) {
        } else {
            message.setText(getString(R.string.msg_no_printer));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_DEVICE_REQUEST_CODE && resultCode == RESULT_OK) {

        }
    }
}
