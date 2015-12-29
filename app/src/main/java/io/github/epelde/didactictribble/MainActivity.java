package io.github.epelde.didactictribble;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private class GenerateTicketTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Service client = GenerateTicketClient.createService(Service.class);
            Call<GenerateTicketResponse> call = client.generateTicket();
            Response<GenerateTicketResponse> apiResponse = null;
            try {
                apiResponse = call.execute();
                Log.i("XXX", "***CODE:" + apiResponse.code());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            GenerateTicketResponse response = apiResponse.body();
            Log.i("XXX", "***TICKETS:" + response.getTickets().size());
            return null;
        }
    }
}
