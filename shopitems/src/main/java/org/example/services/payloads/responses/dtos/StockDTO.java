package org.example.services.payloads.responses.dtos;

import org.example.domains.Stock;
import jakarta.json.bind.annotation.JsonbDateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockDTO {

    public Long id;
    public Long itemId;
    public Integer quantityReceived;
    public BigDecimal unitCostPrice;
    public BigDecimal totalCostPrice;

    public BigDecimal unitSellingPrice;
    public Integer quantityAvailable;
    public Integer newQuantity;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;

    // Constructor to map from Stock entity
    public StockDTO(Stock stock) {
        this.id = stock.id;
        this.itemId = stock.item != null ? stock.item.id : null;
        this.quantityReceived = stock.quantityReceived;
        this.unitCostPrice = stock.unitCostPrice;
        this.totalCostPrice = stock.totalCostPrice;
        this.unitSellingPrice = stock.unitSellingPrice;
        this.quantityAvailable = stock.quantityAvailable;
        this.newQuantity = stock.newQuantity;
        this.expiryDate = stock.expiryDate;
        this.receiveDate = stock.receiveDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }


    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitCostPrice() {
        return unitCostPrice;
    }

    public void setUnitCostPrice(BigDecimal unitCostPrice) {
        this.unitCostPrice = unitCostPrice;
    }


    public BigDecimal getUnitSellingPrice() {
        return unitSellingPrice;
    }

    public void setUnitSellingPrice(BigDecimal unitSellingPrice) {
        this.unitSellingPrice = unitSellingPrice;
    }


    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDate receiveDate) {
        this.receiveDate = receiveDate;
    }
}
