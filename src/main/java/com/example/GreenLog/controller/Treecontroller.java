package com.example.GreenLog.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.GreenLog.api.ApiDtos.CreateTreeRequest;
import com.example.GreenLog.api.ApiDtos.SpeciesSurvivalResponse;
import com.example.GreenLog.api.ApiDtos.TreeResponse;
import com.example.GreenLog.service.Treeservice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trees")
public class Treecontroller {
	private final Treeservice treeService;

	public Treecontroller(Treeservice treeService) { this.treeService = treeService; }

	@PostMapping
	public ResponseEntity<TreeResponse> create(@Valid @RequestBody CreateTreeRequest request) {
		TreeResponse tree = treeService.create(request);
		return ResponseEntity.created(URI.create("/api/trees/" + tree.id())).body(tree);
	}

	@GetMapping
	public List<TreeResponse> all() { return treeService.findAll(); }

	@GetMapping("/{id}")
	public TreeResponse get(@PathVariable Long id) { return treeService.get(id); }

	@PutMapping("/{id}")
	public TreeResponse update(@PathVariable Long id, @Valid @RequestBody CreateTreeRequest request) {
		return treeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		treeService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/due")
	public List<TreeResponse> due(@RequestParam(required = false) LocalDate date) {
		return treeService.due(date == null ? LocalDate.now() : date);
	}

	@GetMapping("/survival/species")
	public List<SpeciesSurvivalResponse> survivalBySpecies() { return treeService.survivalBySpecies(); }
}
