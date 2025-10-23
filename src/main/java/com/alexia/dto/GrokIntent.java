package com.alexia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the user's detected intent and extracted search term.
 */
@Data
public class GrokIntent {

    public enum IntentType {
        PRODUCT_SEARCH,
        BUSINESS_SEARCH,
        LEAD_CAPTURE,
        COMPARE_PRICES,
        GENERAL_QUERY
    }

    @JsonProperty("intent")
    private IntentType intent;

    @JsonProperty("searchTerm")
    private String searchTerm;

    @JsonProperty("confidence")
    private double confidence;

    // Fields for lead capture
    @JsonProperty("hasConsent")
    private Boolean hasConsent;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("city")
    private String city;
}
