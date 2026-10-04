package com.teago.teago.dto;


public class ActiveLandOwnerDTO {

    private Integer landOwnerId;
    private String  displayName;   
    private String  name;          



    public ActiveLandOwnerDTO(Integer landOwnerId, String name) {
        this.landOwnerId = landOwnerId;
        this.name        = name;
        this.displayName = name + " (LO-" + String.format("%03d", landOwnerId) + ")";
    }

    

    public Integer getLandOwnerId() { return landOwnerId; }
    public String  getDisplayName() { return displayName; }
    public String  getName()        { return name; }
}