package com.telnyx.videodemo.models.createToken;

public class CreateTokenRequest {
    private int refresh_token_ttl_secs;
    private int token_ttl_secs;

    public CreateTokenRequest() {
        this.refresh_token_ttl_secs = 3600;
        this.token_ttl_secs = 600;
    }

    public CreateTokenRequest(int refresh_token_ttl_secs, int token_ttl_secs) {
        this.refresh_token_ttl_secs = refresh_token_ttl_secs;
        this.token_ttl_secs = token_ttl_secs;
    }

    public int getRefresh_token_ttl_secs() {
        return refresh_token_ttl_secs;
    }

    public void setRefresh_token_ttl_secs(int refresh_token_ttl_secs) {
        this.refresh_token_ttl_secs = refresh_token_ttl_secs;
    }

    public int getToken_ttl_secs() {
        return token_ttl_secs;
    }

    public void setToken_ttl_secs(int token_ttl_secs) {
        this.token_ttl_secs = token_ttl_secs;
    }
}