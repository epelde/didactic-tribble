package io.github.epelde.didactictribble;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by epelde on 29/12/2015.
 */
public interface Service {

    @GET("/generar_ticket.asp?Pais=ES&Codigo=480010001&Clave=xxxxx&Idioma=ES")
    Call<GenerateTicketResponse> generateTicket();

}
