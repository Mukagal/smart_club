package com.miras.smartclub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "clubs")
public class Club {
    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private String location;
    private Double latitude;
    private Double longitude;
    private String address;
    private String phone;
    private String email;
    private String website;
    private List<PriceItem> prices;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public List<PriceItem> getPrices() { return prices; }
    public void setPrices(List<PriceItem> prices) { this.prices = prices; }

    public static class PriceItem {
        private String category;
        private String service;
        private String price;
        private Integer priceNumber;
        private String unit;
        private Integer durationMinutes;
        private Boolean bookable;
        private String type;
        private String resourceType;
        private String timeWindowStart;
        private String timeWindowEnd;
        private Boolean vipOnly;
        private Integer minSeats;
        private Integer maxSeats;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public Integer getPriceNumber() { return priceNumber; }
        public void setPriceNumber(Integer priceNumber) { this.priceNumber = priceNumber; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Boolean getBookable() { return bookable; }
        public void setBookable(Boolean bookable) { this.bookable = bookable; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public String getTimeWindowStart() { return timeWindowStart; }
        public void setTimeWindowStart(String timeWindowStart) { this.timeWindowStart = timeWindowStart; }
        public String getTimeWindowEnd() { return timeWindowEnd; }
        public void setTimeWindowEnd(String timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }
        public Boolean getVipOnly() { return vipOnly; }
        public void setVipOnly(Boolean vipOnly) { this.vipOnly = vipOnly; }
        public Integer getMinSeats() { return minSeats; }
        public void setMinSeats(Integer minSeats) { this.minSeats = minSeats; }
        public Integer getMaxSeats() { return maxSeats; }
        public void setMaxSeats(Integer maxSeats) { this.maxSeats = maxSeats; }
    }
}
