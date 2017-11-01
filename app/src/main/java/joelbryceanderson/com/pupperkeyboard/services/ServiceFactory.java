package joelbryceanderson.com.pupperkeyboard.services;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by JAnderson on 2/8/17.
 *
 * Service factory to generate backend service for use in activities.
 */

public class ServiceFactory {

    private ServiceFactory(){ }

    public static <T> T createRetrofitService(final Class<T> clazz, final String endPoint) {
        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return restAdapter.create(clazz);
    }
}
