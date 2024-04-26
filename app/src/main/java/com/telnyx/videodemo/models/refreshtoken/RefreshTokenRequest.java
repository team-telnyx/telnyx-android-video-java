package com.telnyx.videodemo.models.refreshtoken;


public class RefreshTokenRequest {
    private String refresh_token;
    private int token_ttl_secs;

    public RefreshTokenRequest(String refresh_token, int token_ttl_secs) {
        this.refresh_token = refresh_token;
        this.token_ttl_secs = token_ttl_secs;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getToken_ttl_secs() {
        return token_ttl_secs;
    }

    public void setToken_ttl_secs(int token_ttl_secs) {
        this.token_ttl_secs = token_ttl_secs;
    }
}
