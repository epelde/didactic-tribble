package io.github.epelde.didactictribble;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 29/12/2015.
 */
public class MainActivity extends AppCompatActivity {

    private Button mGenerateTicketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGenerateTicketButton = (Button) findViewById(R.id.generate_ticket_button);
        mGenerateTicketButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new GenerateTicketTask().execute();
            }
        });
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
