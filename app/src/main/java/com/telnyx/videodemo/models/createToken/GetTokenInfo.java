package com.telnyx.videodemo.models.createToken;

public class GetTokenInfo  {
    private String recort_type;
    private String refresh_token;
    private String refresh_token_expires_at;
    private String token;
    private String token_expires_at;

    public GetTokenInfo(String recort_type, String refresh_token, String refresh_token_expires_at, String token, String token_expires_at) {
        this.recort_type = recort_type;
        this.refresh_token = refresh_token;
        this.refresh_token_expires_at = refresh_token_expires_at;
        this.token = token;
        this.token_expires_at = token_expires_at;
    }

    public String getRecort_type() {
        return recort_type;
    }

    public void setRecort_type(String recort_type) {
        this.recort_type = recort_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefresh_token_expires_at() {
        return refresh_token_expires_at;
    }

    public void setRefresh_token_expires_at(String refresh_token_expires_at) {
        this.refresh_token_expires_at = refresh_token_expires_at;
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