package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epelde on 29/12/2015.
 */
public class GenerateTicketResponse {

    @SerializedName("datos")
    private List<Ticket> tickets;

    public GenerateTicketResponse() {
        tickets = new ArrayList<Ticket>();
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}
