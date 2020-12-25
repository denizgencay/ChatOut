package arsi.dev.chatout;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

    public class ApiClient {
    public static NotificationApiService getApiService() {
        return new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .client(provideClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationApiService.class);
    }

    private static OkHttpClient provideClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(chain -> {
            Request request = chain.request();
            return chain.proceed(request);
        }).build();
    }
}
