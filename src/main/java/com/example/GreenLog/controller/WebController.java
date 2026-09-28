package com.example.GreenLog.controller;

import com.example.GreenLog.api.ApiDtos.CreateCheckinRequest;
import com.example.GreenLog.api.ApiDtos.CreateDriveRequest;
import com.example.GreenLog.api.ApiDtos.CreateTreeRequest;
import com.example.GreenLog.api.ApiDtos.CreateVolunteerRequest;
import com.example.GreenLog.service.Checkinservice;
import com.example.GreenLog.service.Plantationdriveservice;
import com.example.GreenLog.service.Treeservice;
import com.example.GreenLog.service.Volunteerservice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class WebController {
	private final Plantationdriveservice driveService;
	private final Treeservice treeService;
	private final Volunteerservice volunteerService;
	private final Checkinservice checkinService;

	public WebController(Plantationdriveservice driveService, Treeservice treeService,
			Volunteerservice volunteerService, Checkinservice checkinService) {
		this.driveService = driveService;
		this.treeService = treeService;
		this.volunteerService = volunteerService;
		this.checkinService = checkinService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("drives", driveService.findAll());
		model.addAttribute("trees", treeService.findAll());
		model.addAttribute("volunteers", volunteerService.findAll());
		model.addAttribute("checkins", checkinService.findAll());
		model.addAttribute("dueTrees", treeService.due(LocalDate.now()));
		return "home";
	}

	@GetMapping("/drives")
	public String drives(Model model) {
		model.addAttribute("drives", driveService.findAll());
		return "drives";
	}

	@GetMapping("/drives/new")
	public String newDrive() { return "drive-form"; }

	@GetMapping("/drives/{id}/edit")
	public String editDrive(@PathVariable Long id, Model model) {
		model.addAttribute("drive", driveService.get(id));
		return "drive-form";
	}

	@PostMapping("/drives")
	public String createDrive(@RequestParam String name, @RequestParam String location,
			@RequestParam LocalDate driveDate, @RequestParam(required = false) Integer checkInIntervalDays,
			RedirectAttributes redirect) {
		return run(redirect, "Drive created", "drives", () -> driveService.create(
				new CreateDriveRequest(name, location, driveDate, checkInIntervalDays)));
	}

	@PostMapping("/drives/{id}")
	public String updateDrive(@PathVariable Long id, @RequestParam String name, @RequestParam String location,
			@RequestParam LocalDate driveDate, @RequestParam(required = false) Integer checkInIntervalDays,
			RedirectAttributes redirect) {
		return run(redirect, "Drive updated", "drives", () -> driveService.update(id,
				new CreateDriveRequest(name, location, driveDate, checkInIntervalDays)));
	}

	@PostMapping("/drives/{id}/delete")
	public String deleteDrive(@PathVariable Long id, RedirectAttributes redirect) {
		return run(redirect, "Drive deleted", "drives", () -> driveService.delete(id));
	}

	@GetMapping("/trees")
	public String trees(Model model) {
		model.addAttribute("trees", treeService.findAll());
		return "trees";
	}

	@GetMapping("/trees/new")
	public String newTree(Model model) { addTreeOptions(model); return "tree-form"; }

	@GetMapping("/trees/{id}/edit")
	public String editTree(@PathVariable Long id, Model model) {
		model.addAttribute("tree", treeService.findAll().stream().filter(item -> item.id().equals(id)).findFirst()
				.orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tree not found")));
		addTreeOptions(model);
		return "tree-form";
	}

	@PostMapping("/trees")
	public String createTree(@RequestParam String species, @RequestParam String location,
			@RequestParam LocalDate datePlanted, @RequestParam Long driveId,
			@RequestParam Long plantedByVolunteerId, RedirectAttributes redirect) {
		return run(redirect, "Tree recorded", "trees", () -> treeService.create(
				new CreateTreeRequest(species, location, datePlanted, driveId, plantedByVolunteerId)));
	}

	@PostMapping("/trees/{id}")
	public String updateTree(@PathVariable Long id, @RequestParam String species, @RequestParam String location,
			@RequestParam LocalDate datePlanted, @RequestParam Long driveId,
			@RequestParam Long plantedByVolunteerId, RedirectAttributes redirect) {
		return run(redirect, "Tree updated", "trees", () -> treeService.update(id,
				new CreateTreeRequest(species, location, datePlanted, driveId, plantedByVolunteerId)));
	}

	@PostMapping("/trees/{id}/delete")
	public String deleteTree(@PathVariable Long id, RedirectAttributes redirect) {
		return run(redirect, "Tree deleted", "trees", () -> treeService.delete(id));
	}

	@GetMapping("/volunteers")
	public String volunteers(Model model) {
		model.addAttribute("volunteers", volunteerService.leaderboard());
		return "volunteers";
	}

	@GetMapping("/volunteers/new")
	public String newVolunteer() { return "volunteer-form"; }

	@GetMapping("/volunteers/{id}/edit")
	public String editVolunteer(@PathVariable Long id, Model model) {
		model.addAttribute("volunteer", volunteerService.findAll().stream().filter(item -> item.id().equals(id))
				.findFirst().orElseThrow(() -> new ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND, "Volunteer not found")));
		return "volunteer-form";
	}

	@PostMapping("/volunteers")
	public String createVolunteer(@RequestParam String name, @RequestParam String email, RedirectAttributes redirect) {
		return run(redirect, "Volunteer added", "volunteers", () -> volunteerService.create(
				new CreateVolunteerRequest(name, email)));
	}

	@PostMapping("/volunteers/{id}")
	public String updateVolunteer(@PathVariable Long id, @RequestParam String name, @RequestParam String email,
			RedirectAttributes redirect) {
		return run(redirect, "Volunteer updated", "volunteers", () -> volunteerService.update(id,
				new CreateVolunteerRequest(name, email)));
	}

	@PostMapping("/volunteers/{id}/delete")
	public String deleteVolunteer(@PathVariable Long id, RedirectAttributes redirect) {
		return run(redirect, "Volunteer deleted", "volunteers", () -> volunteerService.delete(id));
	}

	@GetMapping("/check-ins")
	public String checkins(Model model) {
		model.addAttribute("checkins", checkinService.findAll());
		model.addAttribute("trees", treeService.findAll());
		addTreeOptions(model);
		return "checkins";
	}

	@GetMapping("/check-ins/{id}/edit")
	public String editCheckin(@PathVariable Long id, Model model) {
		model.addAttribute("checkin", checkinService.findAll().stream().filter(item -> item.id().equals(id))
				.findFirst().orElseThrow(() -> new ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND, "Check-in not found")));
		model.addAttribute("volunteers", volunteerService.findAll());
		return "checkin-form";
	}

	@PostMapping("/check-ins")
	public String createCheckin(@RequestParam Long treeId, @RequestParam Long volunteerId,
			@RequestParam String status, @RequestParam(required = false) String notes,
			@RequestParam(required = false) LocalDateTime checkedInAt, RedirectAttributes redirect) {
		return run(redirect, "Check-in recorded", "check-ins", () -> checkinService.create(
				new CreateCheckinRequest(treeId, volunteerId, status, notes, checkedInAt)));
	}

	@PostMapping("/check-ins/{id}")
	public String updateCheckin(@PathVariable Long id, @RequestParam Long volunteerId,
			@RequestParam(required = false) String notes, RedirectAttributes redirect) {
		return run(redirect, "Check-in details updated", "check-ins", () ->
				checkinService.updateDetails(id, volunteerId, notes));
	}

	@PostMapping("/check-ins/{id}/delete")
	public String deleteCheckin(@PathVariable Long id, RedirectAttributes redirect) {
		return run(redirect, "Check-in deleted", "check-ins", () -> checkinService.delete(id));
	}

	private void addTreeOptions(Model model) {
		model.addAttribute("drives", driveService.findAll());
		model.addAttribute("volunteers", volunteerService.findAll());
	}

	private String run(RedirectAttributes redirect, String success, String destination, Runnable action) {
		try {
			action.run();
			redirect.addFlashAttribute("success", success);
		} catch (RuntimeException exception) {
			String message = exception instanceof ResponseStatusException statusException
					? statusException.getReason() : exception.getMessage();
			redirect.addFlashAttribute("error", message == null ? "Request could not be completed" : message);
		}
		return "redirect:/" + destination;
	}
}