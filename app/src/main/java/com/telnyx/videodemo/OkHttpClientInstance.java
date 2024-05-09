package com.telnyx.videodemo;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class OkHttpClientInstance {

    public static OkHttpClient getOkHttpClient() {
        // Create a logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create an interceptor to add the Authorization header
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                okhttp3.Request original = chain.request();

                // Request customization: add request headers
                okhttp3.Request request = original.newBuilder()
                        .header("Authorization", "Bearer YOUR_API_KEY")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        };

        // Build the OkHttpClient
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build();
    }
}
