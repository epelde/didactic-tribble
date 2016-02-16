package io.github.epelde.didactictribble;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by epelde on 12/02/2016.
 */
public interface KobazuloService {

    @GET("/clientes/fidelizacion/generar_ticket.asp?Pais=ES&Codigo=480040010002&Clave=144339&Idioma=ES")
    public Call<TicketCollection> generateTicket();

    @GET("/clientes/fidelizacion/comprobar_ticket.asp?Pais=ES&Codigo=480040010002&Clave=144339&Idioma=ES")
    public Call<Results> validateTicket(@Query("CodigoTicket") String code);

    class Factory {
        public static KobazuloService create() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://www.kobazulo.net/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit.create(KobazuloService.class);
        }
    }

}
