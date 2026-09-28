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

import com.example.GreenLog.api.ApiDtos.CreateVolunteerRequest;
import com.example.GreenLog.api.ApiDtos.VolunteerResponse;
import com.example.GreenLog.service.Volunteerservice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/volunteers")
public class Volunteercontroller {
	private final Volunteerservice volunteerService;

	public Volunteercontroller(Volunteerservice volunteerService) { this.volunteerService = volunteerService; }

	@PostMapping
	public ResponseEntity<VolunteerResponse> create(@Valid @RequestBody CreateVolunteerRequest request) {
		VolunteerResponse volunteer = volunteerService.create(request);
		return ResponseEntity.created(URI.create("/api/volunteers/" + volunteer.id())).body(volunteer);
	}

	@GetMapping
	public List<VolunteerResponse> findAll() { return volunteerService.findAll(); }

	@GetMapping("/{id}")
	public VolunteerResponse get(@PathVariable Long id) { return volunteerService.get(id); }

	@PutMapping("/{id}")
	public VolunteerResponse update(@PathVariable Long id, @Valid @RequestBody CreateVolunteerRequest request) {
		return volunteerService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		volunteerService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/leaderboard")
	public List<VolunteerResponse> leaderboard() { return volunteerService.leaderboard(); }
}
