package com.planifAI.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.time.LocalDate; // ⬅️ Importamos LocalDate para Fecha_Nacimiento
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // --- Campos de Identidad ---

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // --- Campos de Perfil Extendidos ---

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 10)
    private String genero;

    @Column(name = "nivel_actividad", length = 20)
    private String nivelActividad; // Sedentario/Ligero/Moderado/Activo

    @Column(name = "unidad_medida", length = 10, nullable = false)
    @Builder.Default
    private String unidadMedida = "METRIC"; // METRIC (kg/cm) o IMPERIAL (lb/in)

    // --- Campos de Configuración ---

    @Column(name = "auto_update_pantry", nullable = false)
    @Builder.Default
    private boolean autoUpdatePantry = false;

    // --- Campos de Mantenimiento y Seguridad ---

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}