package com.example.GreenLog.repository;

import com.example.GreenLog.model.Checkin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {
	List<Checkin> findByTree_IdOrderByCheckedInAtDesc(Long treeId);
	List<Checkin> findAllByOrderByCheckedInAtDesc();
	boolean existsByTree_Id(Long treeId);
	boolean existsByVolunteer_Id(Long volunteerId);
	Checkin findFirstByTree_IdOrderByCheckedInAtDesc(Long treeId);
}
