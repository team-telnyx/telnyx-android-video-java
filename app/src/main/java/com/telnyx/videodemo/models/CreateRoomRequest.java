package com.telnyx.videodemo.models;



public class CreateRoomRequest {
    private int max_participant = 10;
    private String unique_name;


    public CreateRoomRequest(String unique_name, int max_participant) {
        this.unique_name = unique_name;
        this.max_participant = max_participant;
    }


    // Getters and Setters
    public int getMaxParticipant() {
        return max_participant;
    }

    public void setMaxParticipant(int max_participant) {
        this.max_participant = max_participant;
    }

    public String getUniqueName() {
        return unique_name;
    }

    public void setUniqueName(String unique_name) {
        this.unique_name = unique_name;
    }
}