package com.miras.smartclub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "seats")
public class Seat {
    @Id
    private String id;
    private String clubId;
    private String label;
    private boolean isVip;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClubId() { return clubId; }
    public void setClubId(String clubId) { this.clubId = clubId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
