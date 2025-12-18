package com.miras.smartclub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "reservations")
public class Reservation {
    @Id
    private String id;
    private String clubId;
    private String userId;
    private List<String> seatIds;
    private Date start;
    private Date end;
    private Integer durationMinutes;
    private String packageId;
    private Integer totalPrice;
    private Date createdAt = new Date();
    private ReservationStatus status = ReservationStatus.PENDING; 
    private Date cancelledAt;
    private String cancelledBy;

    private String paymentIntentId; 


    public enum ReservationStatus { PENDING, ACTIVE, CANCELLED }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClubId() { return clubId; }
    public void setClubId(String clubId) { this.clubId = clubId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = seatIds; }
    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }
    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public Integer getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public Date getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Date cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
}
