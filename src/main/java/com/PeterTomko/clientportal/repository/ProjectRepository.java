package com.PeterTomko.clientportal.repository;

import com.PeterTomko.clientportal.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(Long userId);

    Optional<Project> findByIdAndUserId(Long id, Long userId);
}
