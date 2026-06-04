package com.PeterTomko.clientportal.dto.project;

import com.PeterTomko.clientportal.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Project.Status status;

    private LocalDate deadline;
}
