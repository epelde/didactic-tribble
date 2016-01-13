package io.github.epelde.didactictribble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 29/12/2015.
 */
public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    private Button mGenerateTicketButton;
    private BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGenerateTicketButton = (Button) findViewById(R.id.generate_ticket_button);
        mGenerateTicketButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //new GenerateTicketTask().execute();
                checkBluetoohEnabled();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                printTickets();
            } else {
                Toast.makeText(this, getString(R.string.msg_bluetooh_not_enabled),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBluetoohEnabled() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            btAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();
            if (btAdapter != null) {
                if (!btAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    printTickets();
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_bluetooh_adapter_available),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.msg_bluetooh_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void printTickets() {
        Toast.makeText(this, "Listo para imprimir tickets", Toast.LENGTH_LONG).show();
    }


    private void displayTicket(Ticket ticket) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra("TICKET", ticket);
        startActivity(intent);
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
}
