package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateAuctionRequestDto {
    private String title;
    private String description;
    private Long categoryId;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal minimumIncrement;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getMinimumIncrement() {
        return minimumIncrement;
    }
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
         this.imageUrl = imageUrl;
    }
    public void setMinimumIncrement(BigDecimal minimumIncrement) {
        this.minimumIncrement = minimumIncrement;
    }


}

