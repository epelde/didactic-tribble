package io.github.epelde.didactictribble.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.epelde.didactictribble.GenerateTicketClient;
import io.github.epelde.didactictribble.GenerateTicketResponse;
import io.github.epelde.didactictribble.R;
import io.github.epelde.didactictribble.Service;
import io.github.epelde.didactictribble.Ticket;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 19/01/2016.
 */
public class TicketFragment extends Fragment {

    private static final String LOG_TAG = TicketFragment.class.getSimpleName();

    private Ticket ticket;
    private TextView ticketDate;
    private TextView ticketBusinessName;
    private TextView ticketBusinessAddress;
    private TextView ticketDescription;
    private TextView ticketCode;
    private ImageView mQrImage;
    private Button printButton;
    private PrintTicketListener listener;

    public interface PrintTicketListener {
        public void printTicket(Ticket t);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "* * * onCreate");
        //new GenerateTicketTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.i(LOG_TAG, "* * * onCreateView");
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);
        ticketDate = (TextView) view.findViewById(R.id.date_text_view);
        ticketBusinessName = (TextView) view.findViewById(R.id.business_name_text_view);
        ticketBusinessAddress = (TextView) view.findViewById(R.id.business_address_text_view);
        ticketDescription = (TextView) view.findViewById(R.id.description_text_view);
        ticketCode = (TextView) view.findViewById(R.id.code_text_view);
        mQrImage = (ImageView) view.findViewById(R.id.qr_image_view);
        printButton = (Button) view.findViewById(R.id.print_button);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (ticket != null) {
                    listener.printTicket(ticket);
                //}
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrintTicketListener) {
            listener = (PrintTicketListener) context;
        }
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

    private void displayTicket(Ticket t) {
        ticket = t;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("es", "ES"));
        ticketDate.setText(df.format(t.getDate()));
        ticketBusinessName.setText(t.getBusinessName());
        ticketBusinessAddress.setText(t.getBusinessAddress());
        ticketDescription.setText(t.getDescription());
        ticketCode.setText(t.getCode());
        Picasso.with(getActivity())
                .load(t.getQrCodeUrl())
                .into(mQrImage);
        printButton.setVisibility(View.VISIBLE);
    }

}
