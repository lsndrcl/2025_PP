package com.myapp.UI;

import com.myapp.Transaction;
import com.myapp.TransactionType;

import java.time.LocalDateTime;

public class TransactionFilter {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private TransactionType type;
    private Double minAmount;
    private Double maxAmount;

    public boolean matches(Transaction tx) {
        if (fromDate != null && tx.getTimestamp().isBefore(fromDate)) return false;
        if (toDate != null && tx.getTimestamp().isAfter(toDate)) return false;
        if (type != null && tx.getType() != type) return false;
        if (minAmount != null && tx.getAmount() < minAmount) return false;
        if (maxAmount != null && tx.getAmount() > maxAmount) return false;
        return true;
    }

    // Setters for filter configuration
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
    public void setType(TransactionType type) { this.type = type; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public void setMaxAmount(Double maxAmount) { this.maxAmount = maxAmount; }
}
