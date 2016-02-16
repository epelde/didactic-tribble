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

    @GET("/clientes/fidelizacion/generar_ticket.asp?Pais=ES&Idioma=ES")
    public Call<TicketCollection> generateTicket(@Query("Codigo") String code, @Query("Clave") String key);

    @GET("/clientes/fidelizacion/comprobar_ticket.asp?Pais=ES&Idioma=ES")
    public Call<Results> validateTicket(@Query("Codigo") String code, @Query("Clave") String key,
                                        @Query("CodigoTicket") String codeTicket);

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
