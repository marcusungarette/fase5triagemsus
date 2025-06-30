package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities;

import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_cpf", columnList = "cpf", unique = true),
        @Index(name = "idx_patient_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static PatientJpaEntity fromDomain(Patient patient) {
        return new PatientJpaEntity(
                patient.getId().getValue(),
                patient.getName(),
                patient.getCpf(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                null,
                null
        );
    }


    public Patient toDomain() {
        return Patient.restore(
                PatientId.of(this.id),
                this.name,
                this.cpf,
                this.birthDate,
                this.gender,
                this.phone,
                this.email
        );
    }

    public void updateFromDomain(Patient patient) {
        this.name = patient.getName();
        this.cpf = patient.getCpf();
        this.birthDate = patient.getBirthDate();
        this.gender = patient.getGender();
        this.phone = patient.getPhone();
        this.email = patient.getEmail();
        // updatedAt será atualizado automaticamente pelo @UpdateTimestamp
    }
}