package com.example.GreenLog.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkin")
public class Checkin { 
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private TreeStatus status;
    private LocalDateTime checkedInAt;
    private String notes;

    @ManyToOne(optional = false)
    private Tree tree;

    @ManyToOne(optional = false)
    private Volunteer volunteer;

    public Long getId() { return id; }
    public TreeStatus getStatus() { return status; }
    public void setStatus(TreeStatus status) { this.status = status; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Tree getTree() { return tree; }
    public void setTree(Tree tree) { this.tree = tree; }
    public Volunteer getVolunteer() { return volunteer; }
    public void setVolunteer(Volunteer volunteer) { this.volunteer = volunteer; }
}
