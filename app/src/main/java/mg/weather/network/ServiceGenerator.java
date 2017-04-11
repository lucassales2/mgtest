package mg.weather.network;


import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mg.weather.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class ServiceGenerator {

    public static final String API_BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final Gson gson = new GsonBuilder().create();
    private static Map<Class<?>, Object> map = new HashMap<>();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson));

    public static <S> S getApiService(Class<S> serviceClass) {
        if (map.containsKey(serviceClass)) {
            return (S) map.get(serviceClass);
        } else {
            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .addInterceptor(getInterceptor())
                            .build();
            Retrofit retrofit = builder.client(client).build();
            return retrofit.create(serviceClass);
        }
    }

    @NonNull
    private static Interceptor getInterceptor() {

        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();
                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("appid", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .addQueryParameter("units", "metric")
                        .addQueryParameter("lang", Locale.getDefault().getLanguage())
                        .build();

                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
    }


}
