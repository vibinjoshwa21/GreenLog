package com.example.GreenLog.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.GreenLog.api.ApiDtos.CreateTreeRequest;
import com.example.GreenLog.api.ApiDtos.SpeciesSurvivalResponse;
import com.example.GreenLog.api.ApiDtos.TreeResponse;
import com.example.GreenLog.model.Plantationdrive;
import com.example.GreenLog.model.Tree;
import com.example.GreenLog.model.TreeStatus;
import com.example.GreenLog.model.Volunteer;
import com.example.GreenLog.repository.CheckinRepository;
import com.example.GreenLog.repository.TreeRepository;
import com.example.GreenLog.repository.VolunteerRepository;

@Service
public class Treeservice {
	private final TreeRepository treeRepository;
	private final VolunteerRepository volunteerRepository;
	private final CheckinRepository checkinRepository;
	private final Plantationdriveservice driveService;

	public Treeservice(TreeRepository treeRepository, VolunteerRepository volunteerRepository,
			CheckinRepository checkinRepository, Plantationdriveservice driveService) {
		this.treeRepository = treeRepository;
		this.volunteerRepository = volunteerRepository;
		this.checkinRepository = checkinRepository;
		this.driveService = driveService;
	}

	@Transactional
	public TreeResponse create(CreateTreeRequest request) {
		Plantationdrive drive = driveService.findDrive(request.driveId());
		Volunteer volunteer = volunteerRepository.findById(request.plantedByVolunteerId()).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Volunteer " + request.plantedByVolunteerId() + " was not found"));
		Tree tree = new Tree();
		tree.setSpecies(request.species().trim());
		tree.setLocation(request.location().trim());
		tree.setDatePlanted(request.datePlanted());
		tree.setNextCheckInDate(request.datePlanted().plusDays(drive.getCheckInIntervalDays()));
		tree.setPlantationDrive(drive);
		tree.setPlantedBy(volunteer);
		Tree saved = treeRepository.save(tree);
		driveService.recalculateSurvival(drive);
		return response(saved);
	}

	@Transactional(readOnly = true)
	public List<TreeResponse> due(LocalDate date) {
		return treeRepository.findByNextCheckInDateLessThanEqualAndStatusNot(date, TreeStatus.DEAD)
				.stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public List<TreeResponse> findAll() {
		return treeRepository.findAll().stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public TreeResponse get(Long id) {
		Tree tree = treeRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Tree " + id + " was not found"));
		return response(tree);
	}

	@Transactional
	public TreeResponse update(Long id, CreateTreeRequest request) {
		Tree tree = treeRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Tree " + id + " was not found"));
		Plantationdrive oldDrive = tree.getPlantationDrive();
		Plantationdrive drive = driveService.findDrive(request.driveId());
		Volunteer volunteer = volunteerRepository.findById(request.plantedByVolunteerId()).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Volunteer " + request.plantedByVolunteerId() + " was not found"));
		tree.setSpecies(request.species().trim());
		tree.setLocation(request.location().trim());
		tree.setDatePlanted(request.datePlanted());
		tree.setPlantationDrive(drive);
		tree.setPlantedBy(volunteer);
		if (tree.getStatus() == TreeStatus.UNKNOWN) {
			tree.setNextCheckInDate(request.datePlanted().plusDays(drive.getCheckInIntervalDays()));
		}
		Tree saved = treeRepository.save(tree);
		driveService.recalculateSurvival(oldDrive);
		if (!oldDrive.getId().equals(drive.getId())) driveService.recalculateSurvival(drive);
		return response(saved);
	}

	@Transactional
	public void delete(Long id) {
		Tree tree = treeRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Tree " + id + " was not found"));
		if (checkinRepository.existsByTree_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A tree with check-in history cannot be deleted");
		}
		Plantationdrive drive = tree.getPlantationDrive();
		treeRepository.delete(tree);
		driveService.recalculateSurvival(drive);
	}

	@Transactional(readOnly = true)
	public List<SpeciesSurvivalResponse> survivalBySpecies() {
		Map<String, long[]> counts = new LinkedHashMap<>();
		for (Tree tree : treeRepository.findAll()) {
			long[] count = counts.computeIfAbsent(tree.getSpecies(), ignored -> new long[2]);
			count[0]++;
			if (tree.getStatus() == TreeStatus.ALIVE) count[1]++;
		}
		List<SpeciesSurvivalResponse> responses = new ArrayList<>();
		counts.forEach((species, count) -> responses.add(new SpeciesSurvivalResponse(species, count[0], count[1],
				count[0] == 0 ? 0.0 : Math.round((count[1] * 1000.0) / count[0]) / 10.0)));
		return responses;
	}

	private TreeResponse response(Tree tree) {
		return new TreeResponse(tree.getId(), tree.getSpecies(), tree.getLocation(), tree.getDatePlanted(),
				tree.getNextCheckInDate(), tree.getStatus().name(), tree.getPlantationDrive().getId(),
				tree.getPlantedBy().getId());
	}
}
