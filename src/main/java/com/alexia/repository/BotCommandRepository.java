package com.alexia.repository;

import com.alexia.entity.BotCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de base de datos con comandos del bot.
 */
@Repository
public interface BotCommandRepository extends JpaRepository<BotCommand, Long> {
    
    /**
     * Encuentra comandos por chat ID ordenados por fecha descendente.
     */
    List<BotCommand> findByChatIdOrderByCreatedAtDesc(Long chatId);
    
    /**
     * Cuenta comandos por chat ID.
     */
    long countByChatId(Long chatId);
    
    /**
     * Cuenta comandos por tipo de comando.
     */
    long countByCommand(String command);
    
    /**
     * Obtiene estadísticas de comandos más usados.
     */
    @Query("SELECT bc.command, COUNT(bc) as count FROM BotCommand bc GROUP BY bc.command ORDER BY count DESC")
    List<Object[]> findCommandStatistics();
}
