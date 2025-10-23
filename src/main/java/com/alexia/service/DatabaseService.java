package com.alexia.service;

import com.alexia.constants.Messages;
import com.alexia.dto.ConnectionResultDTO;
import com.alexia.entity.ConnectionTest;
import com.alexia.exception.DatabaseConnectionException;
import com.alexia.factory.ConnectionTestFactory;
import com.alexia.repository.ConnectionTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Servicio para operaciones de base de datos.
 * Maneja la lógica de negocio relacionada con la persistencia.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {

    private final ConnectionTestRepository connectionTestRepository;
    private final ConnectionTestFactory connectionTestFactory;

    /**
     * Prueba la conexión a la base de datos creando un registro de prueba.
     * 
     * @return ConnectionResultDTO con el resultado de la operación
     * @throws DatabaseConnectionException si hay un error de conexión
     */
    public ConnectionResultDTO testConnection() {
        log.debug("Iniciando prueba de conexión a base de datos");
        
        try {
            ConnectionTest saved = createAndSaveTestRecord();
            long totalRecords = getTotalRecordCount();
            
            log.info("Conexión exitosa a Supabase - recordId={}, message={}", 
                    saved.getId(), saved.getMessage());
            log.debug("Total de registros en connection_test: {}", totalRecords);
            
            return ConnectionResultDTO.success(
                saved.getId(),
                totalRecords,
                saved.getMessage()
            );
            
        } catch (DataAccessException e) {
            log.error("Error de acceso a datos - exception={}, message={}", 
                    e.getClass().getSimpleName(), e.getMessage());
            throw new DatabaseConnectionException("Error al conectar con Supabase", e);
        } catch (Exception e) {
            log.error("Error inesperado al probar conexión - exception={}, message={}", 
                    e.getClass().getSimpleName(), e.getMessage());
            return ConnectionResultDTO.error(e.getMessage());
        }
    }

    /**
     * Crea y guarda un registro de prueba en la base de datos.
     * 
     * @return El registro guardado
     */
    private ConnectionTest createAndSaveTestRecord() {
        ConnectionTest test = connectionTestFactory.createTestRecord();
        return connectionTestRepository.save(test);
    }

    /**
     * Obtiene el total de registros en la tabla connection_test.
     * 
     * @return Número total de registros
     */
    private long getTotalRecordCount() {
        return connectionTestRepository.count();
    }
}
