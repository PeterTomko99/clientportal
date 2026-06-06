package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.FileAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileAttachmentServiceTest {

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @InjectMocks
    private FileAttachmentService fileAttachmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileAttachmentService, "uploadDir", "uploads");
    }

    @Test
    void getFilesByProjectAndUser_returnsFiles() {
        List<FileAttachment> files = List.of(new FileAttachment());
        when(fileAttachmentRepository.findByProjectIdAndProjectUserId(1L, 1L)).thenReturn(files);

        List<FileAttachment> result = fileAttachmentService.getFilesByProjectAndUser(1L, 1L);

        assertEquals(files, result);
    }

    @Test
    void getFileByIdAndUser_returnsFileWhenFound() {
        FileAttachment file = new FileAttachment();
        when(fileAttachmentRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.of(file));

        FileAttachment result = fileAttachmentService.getFileByIdAndUser(1L, 1L);

        assertEquals(file, result);
    }

    @Test
    void getFileByIdAndUser_throwsWhenNotFound() {
        when(fileAttachmentRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileAttachmentService.getFileByIdAndUser(1L, 1L));
    }

    @Test
    void delete_deletesFileWhenFound() {
        FileAttachment file = new FileAttachment();
        when(fileAttachmentRepository.findByIdAndProjectUserId(1L, 1L)).thenReturn(Optional.of(file));

        fileAttachmentService.delete(1L, 1L);

        verify(fileAttachmentRepository).delete(file);
    }

    @Test
    void loadResource_throwsWhenFileDoesNotExistOnDisk() {
        FileAttachment attachment = new FileAttachment();
        attachment.setFilePath("nonexistent-" + System.currentTimeMillis() + ".pdf");

        assertThrows(ResourceNotFoundException.class, () -> fileAttachmentService.loadResource(attachment));
    }
}
