package com.example.GreenLog.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record CreateDriveRequest(
            @NotBlank String name,
            @NotBlank String location,
            @NotNull LocalDate driveDate,
            @Min(1) Integer checkInIntervalDays) { }

    public record DriveResponse(Long id, String name, String location, LocalDate driveDate,
            int checkInIntervalDays, double survivalRate) { }

    public record CreateTreeRequest(
            @NotBlank String species,
            @NotBlank String location,
            @NotNull LocalDate datePlanted,
            @NotNull Long driveId,
            @NotNull Long plantedByVolunteerId) { }

    public record TreeResponse(Long id, String species, String location, LocalDate datePlanted,
            LocalDate nextCheckInDate, String status, Long driveId, Long plantedByVolunteerId) { }

    public record CreateVolunteerRequest(
            @NotBlank String name,
            @NotBlank @Email String email) { }

    public record VolunteerResponse(Long id, String name, String email, long treesPlanted) { }

    public record CreateCheckinRequest(
            @NotNull Long treeId,
            @NotNull Long volunteerId,
            @NotBlank String status,
            String notes,
            LocalDateTime checkedInAt) { }

    public record UpdateCheckinRequest(@NotNull Long volunteerId, String notes) { }

    public record CheckinResponse(Long id, Long treeId, Long volunteerId, String status,
            String notes, LocalDateTime checkedInAt) { }

    public record SpeciesSurvivalResponse(String species, long totalTrees, long aliveTrees,
            double survivalRate) { }

    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message) { }
}