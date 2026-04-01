package pawtner_core.pawtner_care_api.pet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pawtner_core.pawtner_care_api.pet.enums.PetStatus;
import pawtner_core.pawtner_care_api.user.entity.User;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "adoption_date")
    private LocalDate adoptionDate;

    @Column(name = "rescued_date")
    private LocalDate rescuedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String photo;

    @Column(length = 500)
    private String videos;

    @Column(name = "is_vaccinated")
    private Boolean isVaccinated;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 100)
    private String race;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PetStatus status;

    @ManyToOne
    @JoinColumn(name = "adopted_by_id")
    private User adoptedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getAdoptionDate() {
        return adoptionDate;
    }

    public void setAdoptionDate(LocalDate adoptionDate) {
        this.adoptionDate = adoptionDate;
    }

    public LocalDate getRescuedDate() {
        return rescuedDate;
    }

    public void setRescuedDate(LocalDate rescuedDate) {
        this.rescuedDate = rescuedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getVideos() {
        return videos;
    }

    public void setVideos(String videos) {
        this.videos = videos;
    }

    public Boolean getIsVaccinated() {
        return isVaccinated;
    }

    public void setIsVaccinated(Boolean isVaccinated) {
        this.isVaccinated = isVaccinated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public PetStatus getStatus() {
        return status;
    }

    public void setStatus(PetStatus status) {
        this.status = status;
    }

    public User getAdoptedBy() {
        return adoptedBy;
    }

    public void setAdoptedBy(User adoptedBy) {
        this.adoptedBy = adoptedBy;
    }
}

