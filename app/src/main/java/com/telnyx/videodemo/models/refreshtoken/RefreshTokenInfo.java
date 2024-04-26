package com.telnyx.videodemo.models.refreshtoken;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenInfo {
    private String token;
    @SerializedName("token_expires_at")
    private String token_expires_at;

    public RefreshTokenInfo(String token, String token_expires_at) {
        this.token = token;
        this.token_expires_at = token_expires_at;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken_expires_at() {
        return token_expires_at;
    }

    public void setToken_expires_at(String token_expires_at) {
        this.token_expires_at = token_expires_at;
    }
}