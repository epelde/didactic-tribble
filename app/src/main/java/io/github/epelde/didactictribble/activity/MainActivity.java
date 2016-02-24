package io.github.epelde.didactictribble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.epelde.didactictribble.Commands;
import io.github.epelde.didactictribble.ImageFetcher;
import io.github.epelde.didactictribble.KobazuloService;
import io.github.epelde.didactictribble.R;
import io.github.epelde.didactictribble.Ticket;
import io.github.epelde.didactictribble.TicketCollection;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 08/02/2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private static final int REQUEST_CODE_ENABLE_BT = 2;

    private BluetoothService service = null;
    private ProgressBar progress;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new BluetoothService(this, mHandler);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        progress = (ProgressBar) findViewById(R.id.progress);
        Button printBtn = (Button) findViewById(R.id.print_button);
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBT()) {
                    setConnection();
                }
            }
        });
        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BarcodeScannerActivity.class));
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
            case REQUEST_CODE_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.toast_msg_must_enable_bluetooth,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.toast_msg_bluetooth_enabled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_conf_printer:
                if (checkBT()) {
                    startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class),
                            REQUEST_CODE_SELECT_DEVICE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkBT() {
        if (service.isDiscovering()) {
            service.cancelDiscovery();
        }
        if (service.isAvailable() == false) {
            Toast.makeText(MainActivity.this, R.string.toast_msg_bluetooth_not_available,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (service.isBTopen() == false) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_CODE_ENABLE_BT);
                return false;
            }
        }
        return true;
    }

    private void setConnection() {
        String address = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.KEY_PRINTER_ADRESS), null);
        if (address == null) {
            Toast.makeText(this, R.string.toast_msg_printer_no_configured, Toast.LENGTH_SHORT).show();
        } else {
            service.start();
            BluetoothDevice device = service.getDevByMac(address);
            if (device != null) {
                progress.setVisibility(View.VISIBLE);
                Toast.makeText(this, R.string.toast_msg_connecting, Toast.LENGTH_SHORT)
                        .show();
                service.connect(device);
            } else {
                Toast.makeText(this, R.string.toast_msg_unable_connect_device, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, R.string.toast_msg_generating_ticket, Toast.LENGTH_SHORT)
                                    .show();
                            new GenerateTicketTask().execute();
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    progress.setVisibility(View.GONE);
                    service.stop();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    progress.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, R.string.toast_msg_unable_connect_device, Toast.LENGTH_SHORT)
                            .show();
                    service.stop();
                    break;
            }
        }
    };

    private class GenerateTicketTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String code = sharedPref.getString("pref_param_code", "");
            String key = sharedPref.getString("pref_param_key", "");
            KobazuloService service = KobazuloService.Factory.create();
            Call<TicketCollection> call = service.generateTicket(code, key);
            try {
                Response<TicketCollection> response = call.execute();
                if (response.isSuccess()) {
                    TicketCollection collection = response.body();
                    if (!collection.getData().isEmpty()) {
                        Ticket t = collection.getData().get(0);
                        byte[] imgBytes = new ImageFetcher().getUrlBytes(t.getCodeURL());
                        t.setImageFile(imgBytes);
                        printTicket(t);
                    }
                } else {
                    return -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }

        protected void onPostExecute(Integer code) {
            progress.setVisibility(View.GONE);
            if (code == -1) {
                Toast.makeText(MainActivity.this, R.string.toast_msg_printing_ticket_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void printTicket(Ticket t) {
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            tempFile = File.createTempFile("chart", ".png");
            fos = new FileOutputStream(tempFile);
            fos.write(t.getImageFile());
            fos.flush();
            fos.close();
            if (tempFile.exists()) {
                service.sendMessage("--------------------------------\n\n", "GBK");
                service.write(Commands.PRINT_SPEED);
                service.write(Commands.INTERNATIONAL_CHARACTERSET);
                service.write(Commands.PRINT_ALIGNMENT_CENTER);
                byte[] cmd = new byte[3];
                cmd[0] = 0x1b;
                cmd[1] = 0x21;
                cmd[2] &= 0xEF;
                service.write(cmd);
                service.sendMessage(t.getDate() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_ON);
                service.sendMessage(t.getName() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_OFF);
                service.sendMessage(t.getAddress() + "\n", "GBK");
                cmd[2] |= 0x10;
                service.write(cmd);
                service.write(Commands.FONT_BOLD_ON);
                service.sendMessage(t.getDescription() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_OFF);
                cmd[2] &= 0xEF;
                service.write(cmd);
                service.sendMessage(t.getCode() + "\n", "GBK");
                PrintPic pg = new PrintPic();
                pg.initCanvas(384);
                pg.initPaint();
                pg.drawImage(0, 0, tempFile.getAbsolutePath());
                service.write(pg.printDraw());
                service.sendMessage("--------------------------------\n\n", "GBK");
                byte[] print = new byte[1];
                cmd[0] = 0x1B;
                cmd[1] = 0x64;
                cmd[2] = 0x2;
                service.write(cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tempFile != null) {
            tempFile.delete();
        }
        service.stop();
    }


}
