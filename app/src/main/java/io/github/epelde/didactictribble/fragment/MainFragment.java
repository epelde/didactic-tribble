package io.github.epelde.didactictribble.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.github.epelde.didactictribble.R;
import io.github.epelde.didactictribble.activity.TicketActivity;

/**
 * Created by epelde on 19/01/2016.
 */
public class MainFragment extends Fragment {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private Button generateButton;
    private Button validateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        generateButton = (Button) view.findViewById(R.id.generate_ticket_button);
        validateButton = (Button) view.findViewById(R.id.validate_ticket_button);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TicketActivity.class));
            }
        });
        return view;
    }
}
