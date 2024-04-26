package com.telnyx.videodemo;

import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpUrl baseUrl = new HttpUrl.Builder().scheme("https").host("api.telnyx.com").encodedPath("/v2/").build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(OkHttpClientInstance.getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

