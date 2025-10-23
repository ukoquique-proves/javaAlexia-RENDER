package com.alexia.service;

import com.alexia.dto.TelegramMessageDTO;
import com.alexia.entity.TelegramMessage;
import com.alexia.repository.TelegramMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para TelegramService.
 * Verifica la lógica de negocio del servicio de Telegram.
 */
@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @Mock
    private TelegramMessageRepository repository;

    @InjectMocks
    private TelegramService service;

    private TelegramMessageDTO testDTO;
    private TelegramMessage testEntity;

    @BeforeEach
    void setUp() {
        testDTO = TelegramMessageDTO.builder()
                .chatId(12345L)
                .userName("testuser")
                .firstName("Test")
                .lastName("User")
                .messageText("Hello Bot")
                .build();

        testEntity = TelegramMessage.builder()
                .id(1L)
                .chatId(12345L)
                .userName("testuser")
                .firstName("Test")
                .lastName("User")
                .messageText("Hello Bot")
                .build();
    }

    @Test
    void shouldSaveMessageSuccessfully() {
        // Given
        when(repository.save(any(TelegramMessage.class))).thenReturn(testEntity);

        // When
        TelegramMessage result = service.saveMessage(testDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChatId()).isEqualTo(12345L);
        assertThat(result.getMessageText()).isEqualTo("Hello Bot");
        verify(repository, times(1)).save(any(TelegramMessage.class));
    }

    @Test
    void shouldGetTotalMessageCount() {
        // Given
        when(repository.count()).thenReturn(42L);

        // When
        long count = service.getTotalMessageCount();

        // Then
        assertThat(count).isEqualTo(42L);
        verify(repository, times(1)).count();
    }
}
