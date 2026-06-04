package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getProjectsByUser_returnsProjectsForUser() {
        List<Project> projects = List.of(new Project());
        when(projectRepository.findByUserId(1L)).thenReturn(projects);

        List<Project> result = projectService.getProjectsByUser(1L);

        assertEquals(projects, result);
        verify(projectRepository).findByUserId(1L);
    }

    @Test
    void getProjectByIdAndUser_returnsProjectWhenFound() {
        Project project = new Project();
        when(projectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectByIdAndUser(1L, 1L);

        assertEquals(project, result);
    }

    @Test
    void getProjectByIdAndUser_throwsWhenNotFound() {
        when(projectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectByIdAndUser(1L, 1L));
    }

    @Test
    void delete_deletesProjectWhenFound() {
        Project project = new Project();
        when(projectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(project));

        projectService.delete(1L, 1L);

        verify(projectRepository).delete(project);
    }

    @Test
    void delete_throwsWhenProjectNotFound() {
        when(projectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.delete(1L, 1L));
    }
}
