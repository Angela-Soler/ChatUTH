package com.proyecto.droidnotes.models;

import java.util.ArrayList;

public class Chat {

    private String id;
    private long timestamp;
    private ArrayList<String>ids;
    private int idNotification;
    private boolean isMultichat;
    private String groupName;
    private String  groupImage;

    // CONSTRUCTOR VACIO
    public Chat() {
    }

    //CONTRUCTOR CON LOS TRES CAMPOS


    public Chat(String id, long timestamp, ArrayList<String> ids, int idNotification, boolean isMultichat, String groupName, String groupImage) {
        this.id = id;
        this.timestamp = timestamp;
        this.ids = ids;
        this.idNotification = idNotification;
        this.isMultichat = isMultichat;
        this.groupName = groupName;
        this.groupImage = groupImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }

    public boolean isMultichat() {
        return isMultichat;
    }

    public void setMultichat(boolean multichat) {
        isMultichat = multichat;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }
}
