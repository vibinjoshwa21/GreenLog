package com.example.GreenLog.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.GreenLog.api.ApiDtos.CreateVolunteerRequest;
import com.example.GreenLog.api.ApiDtos.VolunteerResponse;
import com.example.GreenLog.model.Volunteer;
import com.example.GreenLog.repository.CheckinRepository;
import com.example.GreenLog.repository.TreeRepository;
import com.example.GreenLog.repository.VolunteerRepository;

@Service
public class Volunteerservice {
	private final VolunteerRepository volunteerRepository;
	private final TreeRepository treeRepository;
	private final CheckinRepository checkinRepository;

	public Volunteerservice(VolunteerRepository volunteerRepository, TreeRepository treeRepository,
			CheckinRepository checkinRepository) {
		this.volunteerRepository = volunteerRepository;
		this.treeRepository = treeRepository;
		this.checkinRepository = checkinRepository;
	}

	@Transactional
	public VolunteerResponse create(CreateVolunteerRequest request) {
		if (volunteerRepository.existsByEmailIgnoreCase(request.email().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A volunteer with that email already exists");
		}
		Volunteer volunteer = new Volunteer();
		volunteer.setName(request.name().trim());
		volunteer.setEmail(request.email().trim());
		return response(volunteerRepository.save(volunteer));
	}

	@Transactional(readOnly = true)
	public List<VolunteerResponse> findAll() {
		return volunteerRepository.findAll().stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public VolunteerResponse get(Long id) {
		Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer " + id + " was not found"));
		return response(volunteer);
	}

	@Transactional(readOnly = true)
	public List<VolunteerResponse> leaderboard() {
		return volunteerRepository.findAll().stream().map(this::response)
				.sorted(Comparator.comparingLong(VolunteerResponse::treesPlanted).reversed()
						.thenComparing(VolunteerResponse::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional
	public VolunteerResponse update(Long id, CreateVolunteerRequest request) {
		Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer " + id + " was not found"));
		if (volunteerRepository.existsByEmailIgnoreCase(request.email().trim())
				&& !volunteer.getEmail().equalsIgnoreCase(request.email().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A volunteer with that email already exists");
		}
		volunteer.setName(request.name().trim());
		volunteer.setEmail(request.email().trim());
		return response(volunteerRepository.save(volunteer));
	}

	@Transactional
	public void delete(Long id) {
		Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer " + id + " was not found"));
		if (treeRepository.existsByPlantedBy_Id(id) || checkinRepository.existsByVolunteer_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A volunteer linked to trees or check-ins cannot be deleted");
		}
		volunteerRepository.delete(volunteer);
	}

	private VolunteerResponse response(Volunteer volunteer) {
		return new VolunteerResponse(volunteer.getId(), volunteer.getName(), volunteer.getEmail(),
				treeRepository.countByPlantedBy_Id(volunteer.getId()));
	}
}
