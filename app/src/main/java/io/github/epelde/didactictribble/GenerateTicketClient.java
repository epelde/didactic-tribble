package io.github.epelde.didactictribble;

import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by epelde on 29/12/2015.
 */
public class GenerateTicketClient {

    private static final String API_BASE_URL = "http://www.kobazulo.net/clientes/fidelizacion";
    private static OkHttpClient httpClient = new OkHttpClient();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(
                    new GsonBuilder().setDateFormat("MM/dd/yyyy HH:mm:ss a")
                            .create()

            ));

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }

}
