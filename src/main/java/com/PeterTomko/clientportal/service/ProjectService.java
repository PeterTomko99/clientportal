package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<Project> getProjectsByUser(Long userId) {
        return projectRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjectsByUser(Long userId, Pageable pageable) {
        return projectRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Project getProjectByIdAndUser(Long id, Long userId) {
        return projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    @Transactional
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Project project = getProjectByIdAndUser(id, userId);
        projectRepository.delete(project);
    }
}
