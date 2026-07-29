package com.vela.im.service.application.utils;

import com.vela.im.service.message.infrastructure.persistence.mapper.ImFileMapper;
import com.vela.im.shared.config.ImServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService - 文件存储服务")
class FileStorageServiceTest {

    @Mock private ImFileMapper fileMapper;
    @Mock private ImServerProperties appConfig;

    @Test
    @DisplayName("文件过大应抛异常")
    void fileTooLargeThrows() {
        ImServerProperties.FileConfig fileConfig = new ImServerProperties.FileConfig();
        fileConfig.setUploadDir("./test-uploads");
        fileConfig.setMaxImageSize(100L);
        lenient().when(appConfig.getFile()).thenReturn(fileConfig);

        assertThrows(RuntimeException.class, () -> {
            new FileStorageService(fileMapper, appConfig);
        });
    }
}
