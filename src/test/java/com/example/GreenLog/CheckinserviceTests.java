package com.example.GreenLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.example.GreenLog.api.ApiDtos.CreateCheckinRequest;
import com.example.GreenLog.model.Checkin;
import com.example.GreenLog.model.Plantationdrive;
import com.example.GreenLog.model.Tree;
import com.example.GreenLog.model.TreeStatus;
import com.example.GreenLog.model.Volunteer;
import com.example.GreenLog.repository.CheckinRepository;
import com.example.GreenLog.repository.Plantationdriverepository;
import com.example.GreenLog.repository.TreeRepository;
import com.example.GreenLog.repository.VolunteerRepository;
import com.example.GreenLog.service.Checkinservice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:greenlog;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class CheckinserviceTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private Checkinservice checkinService;
    @Autowired private CheckinRepository checkinRepository;
    @Autowired private TreeRepository treeRepository;
    @Autowired private Plantationdriverepository driveRepository;
    @Autowired private VolunteerRepository volunteerRepository;

    @AfterEach
    void cleanDatabase() {
        checkinRepository.deleteAll();
        treeRepository.deleteAll();
        driveRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    @Test
    void checkInUpdatesSurvivalAndDeadTreesCannotBeCheckedInAgain() {
        Plantationdrive drive = new Plantationdrive();
        drive.setName("Campus planting");
        drive.setLocation("North lawn");
        drive.setDriveDate(LocalDate.of(2026, 8, 1));
        drive.setCheckInIntervalDays(14);
        drive = driveRepository.save(drive);

        Volunteer volunteer = new Volunteer();
        volunteer.setName("Ari");
        volunteer.setEmail("ari@example.test");
        volunteer = volunteerRepository.save(volunteer);

        Tree tree = new Tree();
        tree.setSpecies("Oak");
        tree.setLocation("North lawn, plot 2");
        tree.setDatePlanted(LocalDate.of(2026, 8, 1));
        tree.setNextCheckInDate(LocalDate.of(2026, 8, 15));
        tree.setPlantationDrive(drive);
        tree.setPlantedBy(volunteer);
        tree = treeRepository.save(tree);
        Long treeId = tree.getId();
        Long volunteerId = volunteer.getId();

        LocalDateTime checkedInAt = LocalDateTime.of(2026, 9, 1, 12, 0);
        checkinService.create(new CreateCheckinRequest(tree.getId(), volunteer.getId(), "ALIVE", null, checkedInAt));
        Tree aliveTree = treeRepository.findById(tree.getId()).orElseThrow();
        assertEquals(TreeStatus.ALIVE, aliveTree.getStatus());
        assertEquals(LocalDate.of(2026, 9, 15), aliveTree.getNextCheckInDate());
        assertEquals(100.0, driveRepository.findById(drive.getId()).orElseThrow().getSurvivalRate());

        checkinService.create(new CreateCheckinRequest(tree.getId(), volunteer.getId(), "DEAD", null,
                checkedInAt.plusDays(14)));
        Tree deadTree = treeRepository.findById(tree.getId()).orElseThrow();
        assertEquals(TreeStatus.DEAD, deadTree.getStatus());
        assertNull(deadTree.getNextCheckInDate());
        assertEquals(0.0, driveRepository.findById(drive.getId()).orElseThrow().getSurvivalRate());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> checkinService.create(new CreateCheckinRequest(treeId, volunteerId, "ALIVE",
                        null, checkedInAt.plusDays(28))));
        assertEquals(400, exception.getStatusCode().value());
        assertEquals(2, checkinRepository.count());
    }

        @Test
        void apiSupportsDrivePlantingCheckInSurvivalDueDatesAndLeaderboard() throws Exception {
        String volunteerBody = mockMvc.perform(post("/api/volunteers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mina\",\"email\":\"mina@example.test\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long volunteerId = objectMapper.readTree(volunteerBody).get("id").asLong();
        assertEquals(1, objectMapper.readTree(mockMvc.perform(get("/api/volunteers"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size());

        String driveBody = mockMvc.perform(post("/api/drives")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Autumn planting\",\"location\":\"Quad\","
                    + "\"driveDate\":\"2026-09-20\",\"checkInIntervalDays\":14}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long driveId = objectMapper.readTree(driveBody).get("id").asLong();
        mockMvc.perform(get("/api/drives")).andExpect(status().isOk());
        mockMvc.perform(get("/api/drives/{id}", driveId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/drives/{id}/survival", driveId)).andExpect(status().isOk());

        String treeBody = mockMvc.perform(post("/api/trees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"species\":\"Maple\",\"location\":\"Quad, plot 1\","
                    + "\"datePlanted\":\"2026-09-20\",\"driveId\":" + driveId
                    + ",\"plantedByVolunteerId\":" + volunteerId + "}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long treeId = objectMapper.readTree(treeBody).get("id").asLong();
        JsonNode dueBeforeCheckin = objectMapper.readTree(mockMvc.perform(get("/api/trees/due")
                .param("date", "2026-10-04"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, dueBeforeCheckin.size());
        mockMvc.perform(get("/api/trees/survival/species")).andExpect(status().isOk());

        mockMvc.perform(post("/api/check-ins").contentType(MediaType.APPLICATION_JSON)
                .content("{\"treeId\":" + treeId + ",\"volunteerId\":" + volunteerId
                    + ",\"status\":\"ALIVE\",\"checkedInAt\":\"2026-10-04T12:00:00\"}"))
            .andExpect(status().isCreated());
        JsonNode checkins = objectMapper.readTree(mockMvc.perform(get("/api/check-ins/tree/{treeId}", treeId))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, checkins.size());
        JsonNode leaderboard = objectMapper.readTree(mockMvc.perform(get("/api/volunteers/leaderboard"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, leaderboard.get(0).get("treesPlanted").asInt());
        assertEquals(100.0, objectMapper.readTree(mockMvc.perform(get("/api/drives/{id}/survival", driveId))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
            .get("survivalRate").asDouble());

        mockMvc.perform(post("/api/check-ins").contentType(MediaType.APPLICATION_JSON)
                .content("{\"treeId\":" + treeId + ",\"volunteerId\":" + volunteerId
                    + ",\"status\":\"DEAD\",\"checkedInAt\":\"2026-10-18T12:00:00\"}"))
            .andExpect(status().isCreated());
        String rejectedBody = mockMvc.perform(post("/api/check-ins").contentType(MediaType.APPLICATION_JSON)
                .content("{\"treeId\":" + treeId + ",\"volunteerId\":" + volunteerId
                    + ",\"status\":\"ALIVE\"}"))
            .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
        assertEquals(true, objectMapper.readTree(rejectedBody).get("message").asText().contains("marked dead"));
        assertEquals(0, objectMapper.readTree(mockMvc.perform(get("/api/trees/due")
                .param("date", "2026-12-01"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).size());
        assertEquals(0.0, objectMapper.readTree(mockMvc.perform(get("/api/trees/survival/species"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
            .get(0).get("survivalRate").asDouble());
        }

        @Test
        void thymeleafPagesRenderAndDriveCrudFormsWork() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
        mockMvc.perform(get("/drives")).andExpect(status().isOk()).andExpect(view().name("drives"));
        mockMvc.perform(get("/drives/new")).andExpect(status().isOk()).andExpect(view().name("drive-form"));
        mockMvc.perform(get("/trees")).andExpect(status().isOk()).andExpect(view().name("trees"));
        mockMvc.perform(get("/trees/new")).andExpect(status().isOk()).andExpect(view().name("tree-form"));
        mockMvc.perform(get("/volunteers")).andExpect(status().isOk()).andExpect(view().name("volunteers"));
        mockMvc.perform(get("/volunteers/new")).andExpect(status().isOk()).andExpect(view().name("volunteer-form"));
        mockMvc.perform(get("/check-ins")).andExpect(status().isOk()).andExpect(view().name("checkins"));

        mockMvc.perform(post("/drives").param("name", "UI garden")
                .param("location", "East courtyard").param("driveDate", "2026-09-28")
                .param("checkInIntervalDays", "21"))
            .andExpect(status().is3xxRedirection());
        Plantationdrive drive = driveRepository.findAll().stream()
            .filter(item -> "UI garden".equals(item.getName())).findFirst().orElseThrow();
        mockMvc.perform(get("/drives/{id}/edit", drive.getId()))
            .andExpect(status().isOk()).andExpect(view().name("drive-form"));
        mockMvc.perform(post("/drives/{id}", drive.getId()).param("name", "Updated garden")
                .param("location", "West courtyard").param("driveDate", "2026-09-28")
                .param("checkInIntervalDays", "21"))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/drives")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Updated garden")));
        mockMvc.perform(post("/drives/{id}/delete", drive.getId())).andExpect(status().is3xxRedirection());
        assertEquals(0, driveRepository.count());
        }

        @Test
        void treeVolunteerAndCheckinUiFormsSupportCreateEditAndDelete() throws Exception {
        mockMvc.perform(post("/drives").param("name", "UI drive")
                .param("location", "Garden").param("driveDate", "2026-09-28")
                .param("checkInIntervalDays", "30"))
            .andExpect(status().is3xxRedirection());
        Long driveId = driveRepository.findAll().get(0).getId();

        mockMvc.perform(post("/volunteers").param("name", "UI volunteer")
                .param("email", "ui-volunteer@example.test"))
            .andExpect(status().is3xxRedirection());
        Long volunteerId = volunteerRepository.findAll().get(0).getId();
        mockMvc.perform(get("/volunteers/{id}/edit", volunteerId)).andExpect(status().isOk());
        mockMvc.perform(post("/volunteers/{id}", volunteerId).param("name", "Updated volunteer")
                .param("email", "ui-volunteer@example.test"))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/trees").param("species", "Birch").param("location", "Garden bed 1")
                .param("datePlanted", "2026-09-28").param("driveId", driveId.toString())
                .param("plantedByVolunteerId", volunteerId.toString()))
            .andExpect(status().is3xxRedirection());
        Long treeId = treeRepository.findAll().get(0).getId();
        mockMvc.perform(get("/trees/{id}/edit", treeId)).andExpect(status().isOk());
        mockMvc.perform(post("/trees/{id}", treeId).param("species", "Silver birch")
                .param("location", "Garden bed 2").param("datePlanted", "2026-09-28")
                .param("driveId", driveId.toString()).param("plantedByVolunteerId", volunteerId.toString()))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/check-ins").param("treeId", treeId.toString())
                .param("volunteerId", volunteerId.toString()).param("status", "ALIVE")
                .param("checkedInAt", "2026-10-01T10:30").param("notes", "Healthy"))
            .andExpect(status().is3xxRedirection());
        Long checkinId = checkinRepository.findAll().get(0).getId();
        mockMvc.perform(get("/check-ins/{id}/edit", checkinId)).andExpect(status().isOk());
        mockMvc.perform(post("/check-ins/{id}", checkinId).param("volunteerId", volunteerId.toString())
                .param("notes", "Checked and healthy"))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/check-ins/{id}/delete", checkinId)).andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/trees/{id}/delete", treeId)).andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/volunteers/{id}/delete", volunteerId)).andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/drives/{id}/delete", driveId)).andExpect(status().is3xxRedirection());
        assertEquals(0, driveRepository.count());
        assertEquals(0, volunteerRepository.count());
        assertEquals(0, treeRepository.count());
        assertEquals(0, checkinRepository.count());
        }
}