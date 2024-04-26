package com.telnyx.videodemo;

import com.telnyx.videodemo.models.CreateRoomRequest;
import com.telnyx.videodemo.models.CreateRoomResponse;
import com.telnyx.videodemo.models.DataResponse;
import com.telnyx.videodemo.models.createToken.CreateTokenRequest;
import com.telnyx.videodemo.models.createToken.GetTokenInfo;
import com.telnyx.videodemo.models.refreshtoken.RefreshTokenInfo;
import com.telnyx.videodemo.models.refreshtoken.RefreshTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // Create a room with the given details (equivalent to POST https://api.telnyx.com/v2/rooms)
    @POST("rooms")
    Call<CreateRoomResponse> createRoom(@Body CreateRoomRequest roomDetails);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("rooms/{room_id}/actions/generate_join_client_token")
    Call<DataResponse<GetTokenInfo>> createClientToken(
            @Path("room_id") String roomId,
            @Body CreateTokenRequest roomRequest
    );

    @POST("rooms/{room_id}/actions/refresh_client_token")
    Call<DataResponse<RefreshTokenInfo>> refreshClientToken(
            @Path("room_id") String roomId,
            @Body RefreshTokenRequest roomRequest
    );


}