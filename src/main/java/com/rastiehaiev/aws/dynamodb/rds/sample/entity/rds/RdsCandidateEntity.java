package com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds;

import com.rastiehaiev.aws.dynamodb.rds.sample.model.ProgrammingLanguage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@Entity(name = "candidate")
@EqualsAndHashCode(callSuper = false)
public class RdsCandidateEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "uuid", unique = true)
    private UUID uuid;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "programming_language")
    private ProgrammingLanguage programmingLanguage;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ExperienceItemEntity> experiences = new HashSet<>();

    @Column(name = "migrated")
    private boolean migrated;

    @PrePersist
    public void prePersist() {
        uuid = Optional.ofNullable(uuid).orElseGet(UUID::randomUUID);
        experiences.forEach(experience -> experience.setCandidate(this));
    }
}
