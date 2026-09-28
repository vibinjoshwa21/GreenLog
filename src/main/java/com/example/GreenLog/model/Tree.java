package com.example.GreenLog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planted_tree")
public class Tree {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String species;
	private String location;
	private LocalDate datePlanted;
	private LocalDate nextCheckInDate;

	@Enumerated(EnumType.STRING)
	private TreeStatus status = TreeStatus.UNKNOWN;

	@ManyToOne(optional = false)
	private Plantationdrive plantationDrive;

	@ManyToOne(optional = false)
	private Volunteer plantedBy;

	@OneToMany(mappedBy = "tree")
	private List<Checkin> checkIns = new ArrayList<>();

	public Long getId() { return id; }
	public String getSpecies() { return species; }
	public void setSpecies(String species) { this.species = species; }
	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }
	public LocalDate getDatePlanted() { return datePlanted; }
	public void setDatePlanted(LocalDate datePlanted) { this.datePlanted = datePlanted; }
	public LocalDate getNextCheckInDate() { return nextCheckInDate; }
	public void setNextCheckInDate(LocalDate nextCheckInDate) { this.nextCheckInDate = nextCheckInDate; }
	public TreeStatus getStatus() { return status; }
	public void setStatus(TreeStatus status) { this.status = status; }
	public Plantationdrive getPlantationDrive() { return plantationDrive; }
	public void setPlantationDrive(Plantationdrive plantationDrive) { this.plantationDrive = plantationDrive; }
	public Volunteer getPlantedBy() { return plantedBy; }
	public void setPlantedBy(Volunteer plantedBy) { this.plantedBy = plantedBy; }
	public List<Checkin> getCheckIns() { return checkIns; }
}
