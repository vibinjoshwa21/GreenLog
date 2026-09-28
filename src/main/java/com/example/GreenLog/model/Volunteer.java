package com.example.GreenLog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "volunteer")
public class Volunteer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String email;

	@OneToMany(mappedBy = "plantedBy")
	private List<Tree> plantedTrees = new ArrayList<>();

	@OneToMany(mappedBy = "volunteer")
	private List<Checkin> checkIns = new ArrayList<>();

	public Long getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public List<Tree> getPlantedTrees() { return plantedTrees; }
	public List<Checkin> getCheckIns() { return checkIns; }
}
