package io.github.epelde.didactictribble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 15/02/2016.
 */
public class PrintTicketActivity extends AppCompatActivity {

    private static final String LOG_TAG = PrintTicketActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ENABLE_BT = 1;
    private BluetoothService service = null;
    private Boolean connected = Boolean.FALSE;
    private TextView date;
    private TextView name;
    private TextView address;
    private TextView description;
    private TextView code;
    private ImageView qrCode;
    private Button printBtn;
    private Ticket ticket;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_ticket);
        date = (TextView) findViewById(R.id.date_text_view);
        name = (TextView) findViewById(R.id.name_text_view);
        address = (TextView) findViewById(R.id.address_text_view);
        description = (TextView) findViewById(R.id.description_text_view);
        code = (TextView) findViewById(R.id.code_text_view);
        qrCode = (ImageView) findViewById(R.id.qr_image_view);
        printBtn = (Button) findViewById(R.id.print_button);
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printTicket();
            }
        });
        progress = (ProgressBar) findViewById(R.id.progress);
        setResult(Activity.RESULT_CANCELED); // Set result CANCELED in case the user backs out
        service = new BluetoothService(this, mHandler);
        if (service.isDiscovering())
            service.cancelDiscovery();
        if (service.isAvailable() == false) {
            Toast.makeText(this, R.string.toast_msg_bluetooth_not_available, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (service.isBTopen() == false) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_CODE_ENABLE_BT);
            } else {
                setConnection();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_CODE_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.toast_msg_must_enable_bluetooth,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    setConnection();
                }
                break;
        }
    }

    private void setConnection() {
        String address = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.KEY_PRINTER_ADRESS), null);
        Log.i(LOG_TAG, "* * * ADDRESS:" + address);
        if (address == null) {
            Toast.makeText(this, R.string.toast_msg_printer_no_configured, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            BluetoothDevice device  = service.getDevByMac(address);
            service.connect(device);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            connected = Boolean.TRUE;
                            progress.setVisibility(View.VISIBLE);
                            new GenerateTicketTask().execute();
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.toast_msg_connection_lost,
                    Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.toast_msg_unable_connect_device,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    private class GenerateTicketTask extends AsyncTask<Void, Void, Ticket> {
        @Override
        protected Ticket doInBackground(Void... params) {
            Ticket t = null;
            KobazuloService service = KobazuloService.Factory.create();
            Call<TicketCollection> call = service.generateTicket();
            try {
                Response<TicketCollection> response = call.execute();
                TicketCollection collection = response.body();
                if (!collection.getData().isEmpty()) {
                    t = collection.getData().get(0);
                    byte[] imgBytes = new ImageFetcher().getUrlBytes(t.getCodeURL());
                    t.setImageFile(imgBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
                t = null;
            }
            return t;
        }

        protected void onPostExecute(Ticket t) {
            File tempFile = null;
            progress.setVisibility(View.GONE);
            try {
                tempFile = File.createTempFile("chart", ".png");
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(t.getImageFile());
                fos.close();
                if (tempFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                    //Bitmap myBitmap = BitmapFactory.decodeByteArray(t.getImageFile(), 0, 0);
                    qrCode.setImageBitmap(myBitmap);
                    qrCode.setVisibility(View.VISIBLE);
                    SimpleDateFormat formatter = new SimpleDateFormat("d/MM/yyyy HH:mm");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date d = null;
                    try {
                        d = dateFormat.parse(t.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        d = new Date();
                    }
                    date.setText(formatter.format(d));
                    name.setText(t.getName());
                    address.setText(t.getAddress());
                    description.setText(t.getDescription());
                    code.setText(t.getCode());
                    printBtn.setVisibility(ImageView.VISIBLE);
                    ticket = t;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printTicket() {
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            tempFile = File.createTempFile("chart", ".png");
            fos = new FileOutputStream(tempFile);
            fos.write(ticket.getImageFile());
            fos.flush();
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
                service.sendMessage(date.getText() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_ON);
                service.sendMessage(ticket.getName() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_OFF);
                service.sendMessage(ticket.getAddress() + "\n", "GBK");
                cmd[2] |= 0x10;
                service.write(cmd);
                service.write(Commands.FONT_BOLD_ON);
                service.sendMessage(ticket.getDescription() + "\n", "GBK");
                service.write(Commands.FONT_BOLD_OFF);
                cmd[2] &= 0xEF;
                service.write(cmd);
                service.sendMessage(ticket.getCode() + "\n", "GBK");

                PrintPic pg = new PrintPic();
                pg.initCanvas(384);
                pg.initPaint();
                pg.drawImage(0, 0, tempFile.getAbsolutePath());
                service.write(pg.printDraw());
                service.sendMessage("--------------------------------\n\n", "GBK");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
