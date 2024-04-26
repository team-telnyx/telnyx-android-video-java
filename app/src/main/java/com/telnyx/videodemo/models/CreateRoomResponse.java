package com.telnyx.videodemo.models;

import com.google.gson.annotations.SerializedName;

public class CreateRoomResponse {
    @SerializedName("data")
    private RoomDetails roomDetails;

    // Getters and Setters
    public RoomDetails getRoomDetails() {
        return roomDetails;
    }

    public void setRoomDetails(RoomDetails roomDetails) {
        this.roomDetails = roomDetails;
    }
}

