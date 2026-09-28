package com.example.GreenLog.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.GreenLog.api.ApiDtos.CheckinResponse;
import com.example.GreenLog.api.ApiDtos.CreateCheckinRequest;
import com.example.GreenLog.model.Checkin;
import com.example.GreenLog.model.Tree;
import com.example.GreenLog.model.TreeStatus;
import com.example.GreenLog.model.Volunteer;
import com.example.GreenLog.repository.CheckinRepository;
import com.example.GreenLog.repository.TreeRepository;
import com.example.GreenLog.repository.VolunteerRepository;

@Service
public class Checkinservice {
	private final CheckinRepository checkinRepository;
	private final TreeRepository treeRepository;
	private final VolunteerRepository volunteerRepository;
	private final Plantationdriveservice driveService;

	public Checkinservice(CheckinRepository checkinRepository, TreeRepository treeRepository,
			VolunteerRepository volunteerRepository, Plantationdriveservice driveService) {
		this.checkinRepository = checkinRepository;
		this.treeRepository = treeRepository;
		this.volunteerRepository = volunteerRepository;
		this.driveService = driveService;
	}

	@Transactional
	public CheckinResponse create(CreateCheckinRequest request) {
		Tree tree = treeRepository.findById(request.treeId()).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Tree " + request.treeId() + " was not found"));
		if (tree.getStatus() == TreeStatus.DEAD) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Tree " + tree.getId() + " is marked dead and cannot receive further check-ins");
		}
		TreeStatus status;
		try {
			status = TreeStatus.valueOf(request.status().trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in status must be ALIVE or DEAD");
		}
		if (status == TreeStatus.UNKNOWN) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in status must be ALIVE or DEAD");
		}
		Volunteer volunteer = volunteerRepository.findById(request.volunteerId()).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Volunteer " + request.volunteerId() + " was not found"));
		LocalDateTime checkedInAt = request.checkedInAt() == null ? LocalDateTime.now() : request.checkedInAt();

		Checkin checkin = new Checkin();
		checkin.setTree(tree);
		checkin.setVolunteer(volunteer);
		checkin.setStatus(status);
		checkin.setNotes(request.notes());
		checkin.setCheckedInAt(checkedInAt);
		Checkin saved = checkinRepository.save(checkin);

		tree.setStatus(status);
		tree.setNextCheckInDate(status == TreeStatus.ALIVE
				? checkedInAt.toLocalDate().plusDays(tree.getPlantationDrive().getCheckInIntervalDays()) : null);
		treeRepository.save(tree);
		driveService.recalculateSurvival(tree.getPlantationDrive());
		return response(saved);
	}

	@Transactional(readOnly = true)
	public List<CheckinResponse> forTree(Long treeId) {
		if (!treeRepository.existsById(treeId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tree " + treeId + " was not found");
		}
		return checkinRepository.findByTree_IdOrderByCheckedInAtDesc(treeId).stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public List<CheckinResponse> findAll() {
		return checkinRepository.findAllByOrderByCheckedInAtDesc().stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public CheckinResponse get(Long id) {
		Checkin checkin = checkinRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Check-in " + id + " was not found"));
		return response(checkin);
	}

	@Transactional
	public CheckinResponse updateDetails(Long id, Long volunteerId, String notes) {
		Checkin checkin = checkinRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Check-in " + id + " was not found"));
		Volunteer volunteer = volunteerRepository.findById(volunteerId).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer " + volunteerId + " was not found"));
		checkin.setVolunteer(volunteer);
		checkin.setNotes(notes);
		return response(checkinRepository.save(checkin));
	}

	@Transactional
	public void delete(Long id) {
		Checkin checkin = checkinRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Check-in " + id + " was not found"));
		Tree tree = checkin.getTree();
		checkinRepository.delete(checkin);
		checkinRepository.flush();
		Checkin latest = checkinRepository.findFirstByTree_IdOrderByCheckedInAtDesc(tree.getId());
		if (latest == null) {
			tree.setStatus(TreeStatus.UNKNOWN);
			tree.setNextCheckInDate(tree.getDatePlanted().plusDays(tree.getPlantationDrive().getCheckInIntervalDays()));
		} else {
			tree.setStatus(latest.getStatus());
			tree.setNextCheckInDate(latest.getStatus() == TreeStatus.ALIVE
					? latest.getCheckedInAt().toLocalDate().plusDays(tree.getPlantationDrive().getCheckInIntervalDays())
					: null);
		}
		treeRepository.save(tree);
		driveService.recalculateSurvival(tree.getPlantationDrive());
	}

	private CheckinResponse response(Checkin checkin) {
		return new CheckinResponse(checkin.getId(), checkin.getTree().getId(), checkin.getVolunteer().getId(),
				checkin.getStatus().name(), checkin.getNotes(), checkin.getCheckedInAt());
	}
}
