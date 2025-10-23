package com.alexia.service;

import com.alexia.entity.Business;
import com.alexia.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests para BusinessService.
 * Verifica la funcionalidad de búsqueda de negocios, incluyendo geolocalización.
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository repository;

    @InjectMocks
    private BusinessService service;

    private Business business1;
    private Business business2;

    @BeforeEach
    void setUp() {
        // Business 1: Verified business in Bogotá center
        business1 = new Business();
        business1.setId(1L);
        business1.setName("Plásticos Mayorista");
        business1.setCategory("Plásticos");
        business1.setAddress("Calle 100 #15-20, Bogotá");
        business1.setPhone("+57 300 1234567");
        business1.setWhatsapp("+57 300 1234567");
        business1.setInstagram("@plasticosmayorista");
        // TODO: Re-enable when location field is restored
        // business1.setLocation("POINT(-74.0721 4.7110)"); // Bogotá center
        business1.setRating(new BigDecimal("4.5"));
        business1.setIsVerified(true);
        business1.setIsActive(true);
        
        // TODO: Re-enable when businessHours field is restored
        // Business hours
        // Map<String, Object> hours = new HashMap<>();
        // hours.put("monday", Arrays.asList(Map.of("open", "09:00", "close", "18:00")));
        // hours.put("tuesday", Arrays.asList(Map.of("open", "09:00", "close", "18:00")));
        // business1.setBusinessHours(hours);

        // Business 2: Unverified business
        business2 = new Business();
        business2.setId(2L);
        business2.setName("Distribuidora Express");
        business2.setCategory("Distribución");
        business2.setAddress("Carrera 7 #45-30, Bogotá");
        business2.setPhone("+57 301 9876543");
        // TODO: Re-enable when location field is restored
        // business2.setLocation("POINT(-74.0900 4.7200)"); // 2km away
        business2.setRating(new BigDecimal("4.2"));
        business2.setIsVerified(false);
        business2.setIsActive(true);
    }

    @Test
    void shouldFindAllActiveBusinesses() {
        // Given
        List<Business> businesses = Arrays.asList(business1, business2);
        when(repository.findByIsActiveTrueOrderByName()).thenReturn(businesses);

        // When
        List<Business> result = service.getAllActiveBusinesses();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Plásticos Mayorista");
        verify(repository, times(1)).findByIsActiveTrueOrderByName();
    }

    @Test
    void shouldFindBusinessesByCategory() {
        // Given
        List<Business> businesses = Arrays.asList(business1);
        when(repository.findByCategoryIgnoreCase("Plásticos")).thenReturn(businesses);

        // When
        List<Business> result = service.searchByCategory("Plásticos");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Plásticos");
        verify(repository, times(1)).findByCategoryIgnoreCase("Plásticos");
    }

    // TODO: Re-enable when location field is restored
    // @Test
    // void shouldFindNearbyBusinesses() {
    //     // Given
    //     double longitude = -74.0721; // Bogotá center
    //     double latitude = 4.7110;
    //     int radiusMeters = 5000; // 5km
    //     List<Business> nearbyBusinesses = Arrays.asList(business1, business2);
    //     
    //     when(repository.findNearby(longitude, latitude, radiusMeters))
    //         .thenReturn(nearbyBusinesses);
    //
    //     // When
    //     List<Business> result = repository.findNearby(longitude, latitude, radiusMeters);
    //
    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(2);
    //     assertThat(result.get(0).getLocation()).isNotNull();
    //     verify(repository, times(1)).findNearby(longitude, latitude, radiusMeters);
    // }

    // TODO: Re-enable when location field is restored
    // @Test
    // void shouldFindVerifiedNearbyBusinesses() {
    //     // Given
    //     double longitude = -74.0721;
    //     double latitude = 4.7110;
    //     int radiusMeters = 5000;
    //     List<Business> verifiedBusinesses = Arrays.asList(business1); // Only verified
    //     
    //     when(repository.findVerifiedNearby(longitude, latitude, radiusMeters))
    //         .thenReturn(verifiedBusinesses);
    //
    //     // When
    //     List<Business> result = repository.findVerifiedNearby(longitude, latitude, radiusMeters);
    //
    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).getIsVerified()).isTrue();
    //     assertThat(result.get(0).getName()).isEqualTo("Plásticos Mayorista");
    //     verify(repository, times(1)).findVerifiedNearby(longitude, latitude, radiusMeters);
    // }

    // TODO: Re-enable when location field is restored
    // @Test
    // void shouldFindBusinessesByCategoryNearby() {
    //     // Given
    //     String category = "Plásticos";
    //     double longitude = -74.0721;
    //     double latitude = 4.7110;
    //     int radiusMeters = 3000; // 3km
    //     List<Business> categoryBusinesses = Arrays.asList(business1);
    //     
    //     when(repository.findByCategoryNearby(category, longitude, latitude, radiusMeters))
    //         .thenReturn(categoryBusinesses);
    //
    //     // When
    //     List<Business> result = repository.findByCategoryNearby(category, longitude, latitude, radiusMeters);
    //
    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).getCategory()).isEqualTo("Plásticos");
    //     assertThat(result.get(0).getLocation()).isNotNull();
    //     verify(repository, times(1)).findByCategoryNearby(category, longitude, latitude, radiusMeters);
    // }

    // TODO: Re-enable when location and businessHours fields are restored
    // @Test
    // void shouldVerifyBusinessHasGeolocationData() {
    //     // Given - business1 has all Step 9 fields
    //
    //     // Then
    //     assertThat(business1.getLocation()).isNotNull();
    //     assertThat(business1.getBusinessHours()).isNotNull();
    //     assertThat(business1.getWhatsapp()).isNotNull();
    //     assertThat(business1.getInstagram()).isNotNull();
    //     assertThat(business1.getRating()).isNotNull();
    //     assertThat(business1.getIsVerified()).isTrue();
    // }

    // TODO: Re-enable when businessHours field is restored
    // @Test
    // void shouldVerifyBusinessHoursStructure() {
    //     // Given
    //     Map<String, Object> hours = business1.getBusinessHours();
    //
    //     // Then
    //     assertThat(hours).isNotNull();
    //     assertThat(hours).containsKey("monday");
    //     assertThat(hours).containsKey("tuesday");
    //     
    //     @SuppressWarnings("unchecked")
    //     List<Map<String, String>> mondayHours = (List<Map<String, String>>) hours.get("monday");
    //     assertThat(mondayHours).isNotEmpty();
    //     assertThat(mondayHours.get(0)).containsEntry("open", "09:00");
    //     assertThat(mondayHours.get(0)).containsEntry("close", "18:00");
    // }

    @Test
    void shouldCountActiveBusinesses() {
        // Given
        when(repository.countByIsActiveTrue()).thenReturn(10L);

        // When
        long count = repository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(10L);
        verify(repository, times(1)).countByIsActiveTrue();
    }

    @Test
    void shouldFindDistinctCategories() {
        // Given
        List<String> categories = Arrays.asList("Plásticos", "Distribución", "Alimentos");
        when(repository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = repository.findDistinctCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains("Plásticos", "Distribución", "Alimentos");
        verify(repository, times(1)).findDistinctCategories();
    }
}
