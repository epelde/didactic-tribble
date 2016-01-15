package io.github.epelde.didactictribble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 30/12/2015.
 */
public class TicketActivity extends AppCompatActivity {

    private static final String LOG_TAG = TicketActivity.class.getSimpleName();

    private TextView ticketDate;
    private TextView ticketBusinessName;
    private TextView ticketBusinessAddress;
    private TextView ticketDescription;
    private TextView ticketCode;
    private ImageView mQrImage;
    private Button printButton;

    private BluetoothAdapter bluetoohAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        ticketDate = (TextView) findViewById(R.id.date_text_view);
        ticketBusinessName = (TextView) findViewById(R.id.business_name_text_view);
        ticketBusinessAddress = (TextView) findViewById(R.id.business_address_text_view);
        ticketDescription = (TextView) findViewById(R.id.description_text_view);
        ticketCode = (TextView) findViewById(R.id.code_text_view);
        mQrImage = (ImageView) findViewById(R.id.qr_image_view);
        printButton = (Button) findViewById(R.id.print_button);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printTicket();
            }
        });
        new GenerateTicketTask().execute();
    }

    private void displayTicket(Ticket t) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("es", "ES"));
        ticketDate.setText(df.format(t.getDate()));
        ticketBusinessName.setText(t.getBusinessName());
        ticketBusinessAddress.setText(t.getBusinessAddress());
        ticketDescription.setText(t.getDescription());
        ticketCode.setText(t.getCode());
        Picasso.with(this)
                .load(t.getQrCodeUrl())
                .into(mQrImage);
        printButton.setVisibility(View.VISIBLE);
    }

    private class GenerateTicketTask extends AsyncTask<Void, Void, Ticket> {
        @Override
        protected Ticket doInBackground(Void... params) {
            Service client = GenerateTicketClient.createService(Service.class);
            Call<GenerateTicketResponse> call = client.generateTicket();
            Response<GenerateTicketResponse> apiResponse = null;
            try {
                apiResponse = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return apiResponse.body().getTickets().get(0);
        }

        @Override
        protected void onPostExecute(Ticket t) {
            super.onPostExecute(t);
            displayTicket(t);
        }
    }

    private void printTicket() {
        Log.i(LOG_TAG, "* * * Running PrintTicketThread...");
        bluetoohAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = null;
        }

        if (bluetoohAdapter != null) {
            if (bluetoohAdapter.isDiscovering()) {
                bluetoohAdapter.cancelDiscovery();
            }
            Set<BluetoothDevice> pairedDevices = bluetoohAdapter.getBondedDevices();
            Log.i(LOG_TAG, "* * * PAIRED DEVICES " + pairedDevices.size());
            if (pairedDevices.size() == 1) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.i(LOG_TAG, "* * * DEVICE:" + device.getName());
                    bluetoothDevice = device;
                }
                new PrintTicketThread().start();

            }
        }

    }

    private class PrintTicketThread extends Thread {

        public void run() {
            Log.i(LOG_TAG, "* * * Running PrintTicketThread...");
            boolean gotuuid = bluetoothDevice.fetchUuidsWithSdp();
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                if (bluetoothSocket == null) {
                    Log.e(LOG_TAG, "* * * Socket nulo");
                } else {
                    Log.i(LOG_TAG, "* * * " + bluetoothSocket.isConnected());
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage());
            }

            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage());
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                bluetoothSocket = null;
            }
        }
    }

}
