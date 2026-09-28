package com.example.GreenLog.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "plantation_drive")
public class Plantationdrive {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String location;
	private LocalDate driveDate;
	private int checkInIntervalDays = 30;
	private double survivalRate;

	@OneToMany(mappedBy = "plantationDrive")
	private List<Tree> trees = new ArrayList<>();

	public Long getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }
	public LocalDate getDriveDate() { return driveDate; }
	public void setDriveDate(LocalDate driveDate) { this.driveDate = driveDate; }
	public int getCheckInIntervalDays() { return checkInIntervalDays; }
	public void setCheckInIntervalDays(int checkInIntervalDays) { this.checkInIntervalDays = checkInIntervalDays; }
	public double getSurvivalRate() { return survivalRate; }
	public void setSurvivalRate(double survivalRate) { this.survivalRate = survivalRate; }
	public List<Tree> getTrees() { return trees; }
}
