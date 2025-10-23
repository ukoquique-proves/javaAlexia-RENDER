package com.alexia.repository;

import com.alexia.entity.ExternalResultCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalResultCacheRepository extends JpaRepository<ExternalResultCache, Long> {
    
    /**
     * Find cached results by query hash
     */
    List<ExternalResultCache> findByQueryHash(String queryHash);
    
    /**
     * Find cached result by source place ID
     */
    Optional<ExternalResultCache> findBySourcePlaceId(String sourcePlaceId);
    
    /**
     * Find cached results by query hash that are still valid (not expired)
     */
    @Query("SELECT e FROM ExternalResultCache e WHERE e.queryHash = :queryHash AND e.fetchedAt > :expiryTime")
    List<ExternalResultCache> findValidByQueryHash(@Param("queryHash") String queryHash, @Param("expiryTime") LocalDateTime expiryTime);
    
    /**
     * Find all expired cache entries
     */
    @Query("SELECT e FROM ExternalResultCache e WHERE e.fetchedAt < :expiryTime")
    List<ExternalResultCache> findExpiredEntries(@Param("expiryTime") LocalDateTime expiryTime);
}
