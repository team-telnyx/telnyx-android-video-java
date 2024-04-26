package com.telnyx.videodemo.models;

public class OfflineRoom {
    private String roomId;
    private String participant;

    public OfflineRoom(String roomId, String participant) {
        this.roomId = roomId;
        this.participant = participant;
    }

    // getters and setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }
}
