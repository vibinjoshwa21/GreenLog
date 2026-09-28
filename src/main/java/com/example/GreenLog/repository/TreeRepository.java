package com.example.GreenLog.repository;

import com.example.GreenLog.model.Tree;
import com.example.GreenLog.model.TreeStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreeRepository extends JpaRepository<Tree, Long> {
	long countByPlantationDrive_Id(Long driveId);
	long countByPlantationDrive_IdAndStatus(Long driveId, TreeStatus status);
	long countByPlantedBy_Id(Long volunteerId);
	boolean existsByPlantationDrive_Id(Long driveId);
	boolean existsByPlantedBy_Id(Long volunteerId);
	List<Tree> findByPlantationDrive_Id(Long driveId);
	List<Tree> findByNextCheckInDateLessThanEqualAndStatusNot(LocalDate date, TreeStatus status);
}
