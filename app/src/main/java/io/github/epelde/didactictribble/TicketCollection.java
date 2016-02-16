package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epelde on 15/02/2016.
 */
public class TicketCollection {

    @SerializedName("datos")
    private List<Ticket> data;

    public TicketCollection() {
        data = new ArrayList<>();
    }

    public List<Ticket> getData() {
        return data;
    }

    public void setData(List<Ticket> data) {
        this.data = data;
    }
}
