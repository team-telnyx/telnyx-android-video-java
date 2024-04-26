package com.telnyx.videodemo.models;

import com.google.gson.annotations.SerializedName;

public class DataResponse<T> {
    @SerializedName("data")
    private T data;

    public DataResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}