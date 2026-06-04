package com.PeterTomko.clientportal.dto.project;

import com.PeterTomko.clientportal.entity.Project;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Project.Status status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .deadline(project.getDeadline())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
