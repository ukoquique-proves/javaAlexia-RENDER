package com.alexia.service;

import com.alexia.dto.SearchResult;
import com.alexia.entity.Business;
import com.alexia.entity.ExternalResultCache;
import com.alexia.repository.BusinessRepository;
import com.alexia.repository.ExternalResultCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    
    private final BusinessRepository businessRepository;
    private final ExternalResultCacheRepository externalResultCacheRepository;
    private final GooglePlacesService googlePlacesService; // To be implemented
    
    /**
     * Main search method implementing RAG strategy:
     * 1. Search internal database first
     * 2. If insufficient results, fallback to external sources
     * 3. Combine and cite sources
     */
    public SearchResult search(String query, Double latitude, Double longitude, Integer radiusMeters) {
        log.info("Executing RAG search for query: '{}' at location: ({}, {}) with radius: {}m", 
                query, latitude, longitude, radiusMeters);
        
        // Normalize query to remove accents for better matching
        String normalizedQuery = removeAccents(query);
        log.debug("Normalized query from '{}' to '{}'", query, normalizedQuery);
        
        // Step 1: Search internal database
        List<Business> internalResults = searchInternalDatabase(normalizedQuery, latitude, longitude, radiusMeters);
        
        // Determine if we need external results
        boolean needExternalResults = internalResults.size() < 3; // Threshold for external search
        
        List<ExternalResultCache> externalResults = null;
        if (needExternalResults) {
            // Step 2: Fallback to external sources
            externalResults = searchExternalSources(query, latitude, longitude, radiusMeters);
        }
        
        // Step 3: Combine and cite sources
        String source = "internal";
        if (externalResults != null && !externalResults.isEmpty()) {
            source = internalResults.isEmpty() ? "external" : "mixed";
        }
        
        return SearchResult.builder()
                .query(query)
                .source(source)
                .internalResults(internalResults)
                .externalResults(externalResults)
                .internalCount(internalResults.size())
                .externalCount(externalResults != null ? externalResults.size() : 0)
                .build();
    }
    
    /**
     * Search internal database for businesses
     */
    private List<Business> searchInternalDatabase(String query, Double latitude, Double longitude, Integer radiusMeters) {
        log.debug("Searching internal database for query: '{}'", query);

        if (latitude != null && longitude != null && radiusMeters != null) {
            return businessRepository.findNearbyWithCategory(query, longitude, latitude, radiusMeters);
        } else {
            // Simple category search
            return businessRepository.findByCategoryContainingIgnoreCase(query);
        }
    }
    
    /**
     * Search external sources (Google Places, etc.) with caching
     */
    private List<ExternalResultCache> searchExternalSources(String query, Double latitude, Double longitude, Integer radiusMeters) {
        log.debug("Searching external sources for query: '{}'", query);
        
        // Create a hash of the query parameters for caching
        String queryHash = generateQueryHash(query, latitude, longitude, radiusMeters);
        
        // Check cache first
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(24); // 24-hour cache
        List<ExternalResultCache> cachedResults = externalResultCacheRepository.findValidByQueryHash(queryHash, expiryTime);
        
        if (!cachedResults.isEmpty()) {
            log.debug("Found {} cached results for query: '{}'", cachedResults.size(), query);
            return cachedResults;
        }
        
        // If no valid cache, fetch from external source
        log.debug("No valid cache found, fetching from external source for query: '{}'", query);
        List<ExternalResultCache> freshResults = googlePlacesService.searchNearby(query, latitude, longitude, radiusMeters);
        
        // Cache the fresh results
        if (freshResults != null && !freshResults.isEmpty()) {
            for (ExternalResultCache result : freshResults) {
                result.setQueryHash(queryHash);
                externalResultCacheRepository.save(result);
            }
            log.debug("Cached {} fresh results for query: '{}'", freshResults.size(), query);
        }
        
        return freshResults;
    }
    
    /**
     * Generate a hash for query parameters to use as cache key
     */
    private String generateQueryHash(String query, Double latitude, Double longitude, Integer radiusMeters) {
        try {
            String input = query + "|" + latitude + "|" + longitude + "|" + radiusMeters;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate query hash", e);
            return query; // Fallback to query string
        }
    }
    
    /**
     * Clean up expired cache entries
     */
    public void cleanupExpiredCache() {
        log.info("Cleaning up expired cache entries");
        List<ExternalResultCache> expiredEntries = externalResultCacheRepository.findExpiredEntries(LocalDateTime.now());
        if (!expiredEntries.isEmpty()) {
            externalResultCacheRepository.deleteAll(expiredEntries);
            log.info("Deleted {} expired cache entries", expiredEntries.size());
        }
    }
    
    /**
     * Remove accents from a string for better search matching
     */
    private String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        // Normalize to NFD (decomposed form) and remove diacritical marks
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}
