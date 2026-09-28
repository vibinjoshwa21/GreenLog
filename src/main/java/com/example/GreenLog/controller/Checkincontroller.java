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

import com.example.GreenLog.api.ApiDtos.CheckinResponse;
import com.example.GreenLog.api.ApiDtos.CreateCheckinRequest;
import com.example.GreenLog.api.ApiDtos.UpdateCheckinRequest;
import com.example.GreenLog.service.Checkinservice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/check-ins")
public class Checkincontroller {
	private final Checkinservice checkinService;

	public Checkincontroller(Checkinservice checkinService) { this.checkinService = checkinService; }

	@PostMapping
	public ResponseEntity<CheckinResponse> create(@Valid @RequestBody CreateCheckinRequest request) {
		CheckinResponse checkin = checkinService.create(request);
		return ResponseEntity.created(URI.create("/api/check-ins/" + checkin.id())).body(checkin);
	}

	@GetMapping
	public List<CheckinResponse> all() { return checkinService.findAll(); }

	@GetMapping("/{id}")
	public CheckinResponse get(@PathVariable Long id) { return checkinService.get(id); }

	@PutMapping("/{id}")
	public CheckinResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCheckinRequest request) {
		return checkinService.updateDetails(id, request.volunteerId(), request.notes());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		checkinService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/tree/{treeId}")
	public List<CheckinResponse> forTree(@PathVariable Long treeId) { return checkinService.forTree(treeId); }
}
