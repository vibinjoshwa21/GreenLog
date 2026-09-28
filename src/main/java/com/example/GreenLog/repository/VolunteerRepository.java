package com.example.GreenLog.repository;

import com.example.GreenLog.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
	boolean existsByEmailIgnoreCase(String email);
}
