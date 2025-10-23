package com.alexia.service;

import com.alexia.dto.ConnectionResultDTO;
import com.alexia.entity.ConnectionTest;
import com.alexia.factory.ConnectionTestFactory;
import com.alexia.repository.ConnectionTestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para DatabaseService.
 * Verifica la funcionalidad de prueba de conexión a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {

    @Mock
    private ConnectionTestRepository repository;

    @Mock
    private ConnectionTestFactory factory;

    @InjectMocks
    private DatabaseService service;

    @Test
    void shouldTestConnectionSuccessfully() {
        // Given
        ConnectionTest testRecord = new ConnectionTest();
        testRecord.setId(1L);
        testRecord.setMessage("Test de conexión exitoso");

        when(factory.createTestRecord()).thenReturn(testRecord);
        when(repository.save(any(ConnectionTest.class))).thenReturn(testRecord);
        when(repository.count()).thenReturn(5L);

        // When
        ConnectionResultDTO result = service.testConnection();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRecordId()).isEqualTo(1L);
        assertThat(result.getTotalRecords()).isEqualTo(5L);
        verify(factory, times(1)).createTestRecord();
        verify(repository, times(1)).save(any(ConnectionTest.class));
        verify(repository, times(1)).count();
    }
}
