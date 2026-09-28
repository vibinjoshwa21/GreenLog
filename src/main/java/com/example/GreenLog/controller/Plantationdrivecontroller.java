package com.example.GreenLog.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GreenLog.api.ApiDtos.CreateDriveRequest;
import com.example.GreenLog.api.ApiDtos.DriveResponse;
import com.example.GreenLog.service.Plantationdriveservice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drives")
public class Plantationdrivecontroller {
	private final Plantationdriveservice driveService;

	public Plantationdrivecontroller(Plantationdriveservice driveService) {
		this.driveService = driveService;
	}

	@PostMapping
	public ResponseEntity<DriveResponse> create(@Valid @RequestBody CreateDriveRequest request) {
		DriveResponse drive = driveService.create(request);
		return ResponseEntity.created(URI.create("/api/drives/" + drive.id())).body(drive);
	}

	@GetMapping
	public List<DriveResponse> findAll() { return driveService.findAll(); }

	@GetMapping("/{id}")
	public DriveResponse get(@PathVariable Long id) { return driveService.get(id); }

	@PutMapping("/{id}")
	public DriveResponse update(@PathVariable Long id, @Valid @RequestBody CreateDriveRequest request) {
		return driveService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		driveService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/survival")
	public DriveResponse survival(@PathVariable Long id) { return driveService.survival(id); }
}
