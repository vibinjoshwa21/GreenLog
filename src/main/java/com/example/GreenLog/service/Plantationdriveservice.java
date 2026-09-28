package com.example.GreenLog.service;

import com.example.GreenLog.api.ApiDtos.CreateDriveRequest;
import com.example.GreenLog.api.ApiDtos.DriveResponse;
import com.example.GreenLog.model.Plantationdrive;
import com.example.GreenLog.repository.Plantationdriverepository;
import com.example.GreenLog.repository.TreeRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Plantationdriveservice {
	private final Plantationdriverepository driveRepository;
	private final TreeRepository treeRepository;

	public Plantationdriveservice(Plantationdriverepository driveRepository, TreeRepository treeRepository) {
		this.driveRepository = driveRepository;
		this.treeRepository = treeRepository;
	}

	@Transactional
	public DriveResponse create(CreateDriveRequest request) {
		Plantationdrive drive = new Plantationdrive();
		drive.setName(request.name().trim());
		drive.setLocation(request.location().trim());
		drive.setDriveDate(request.driveDate());
		drive.setCheckInIntervalDays(Objects.requireNonNullElse(request.checkInIntervalDays(), 30));
		return response(driveRepository.save(drive));
	}

	@Transactional(readOnly = true)
	public List<DriveResponse> findAll() {
		return driveRepository.findAll().stream().map(this::response).toList();
	}

	@Transactional(readOnly = true)
	public DriveResponse get(Long id) {
		return response(findDrive(id));
	}

	@Transactional(readOnly = true)
	public DriveResponse survival(Long id) {
		return response(findDrive(id));
	}

	@Transactional
	public DriveResponse update(Long id, CreateDriveRequest request) {
		Plantationdrive drive = findDrive(id);
		drive.setName(request.name().trim());
		drive.setLocation(request.location().trim());
		drive.setDriveDate(request.driveDate());
		drive.setCheckInIntervalDays(Objects.requireNonNullElse(request.checkInIntervalDays(), 30));
		return response(driveRepository.save(drive));
	}

	@Transactional
	public void delete(Long id) {
		Plantationdrive drive = findDrive(id);
		if (treeRepository.existsByPlantationDrive_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Remove this drive's trees before deleting the drive");
		}
		driveRepository.delete(drive);
	}

	public Plantationdrive findDrive(Long id) {
		return driveRepository.findById(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Plantation drive " + id + " was not found"));
	}

	public void recalculateSurvival(Plantationdrive drive) {
		long total = treeRepository.countByPlantationDrive_Id(drive.getId());
		long alive = treeRepository.countByPlantationDrive_IdAndStatus(drive.getId(), com.example.GreenLog.model.TreeStatus.ALIVE);
		drive.setSurvivalRate(total == 0 ? 0.0 : Math.round((alive * 1000.0) / total) / 10.0);
		driveRepository.save(drive);
	}

	private DriveResponse response(Plantationdrive drive) {
		return new DriveResponse(drive.getId(), drive.getName(), drive.getLocation(), drive.getDriveDate(),
				drive.getCheckInIntervalDays(), drive.getSurvivalRate());
	}
}
