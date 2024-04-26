package com.telnyx.videodemo.models;

public class RoomDetails {
    private String created_at;
    private String id;
    private int max_participants;
    private String record_type;
    private String unique_name;
    private String updated_at;

    // Getters and Setters
    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxParticipants() {
        return max_participants;
    }

    public void setMaxParticipants(int max_participants) {
        this.max_participants = max_participants;
    }

    public String getRecordType() {
        return record_type;
    }

    public void setRecordType(String record_type) {
        this.record_type = record_type;
    }

    public String getUniqueName() {
        return unique_name;
    }

    public void setUniqueName(String unique_name) {
        this.unique_name = unique_name;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at;
    }
}