package com.alexia.dto;

import com.alexia.entity.Business;
import com.alexia.entity.ExternalResultCache;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String source; // "internal", "external", "mixed"
    private List<Business> internalResults;
    private List<ExternalResultCache> externalResults;
    private int internalCount;
    private int externalCount;
    private String query;
    
    public boolean hasInternalResults() {
        return internalResults != null && !internalResults.isEmpty();
    }
    
    public boolean hasExternalResults() {
        return externalResults != null && !externalResults.isEmpty();
    }
    
    public boolean hasResults() {
        return hasInternalResults() || hasExternalResults();
    }
}
