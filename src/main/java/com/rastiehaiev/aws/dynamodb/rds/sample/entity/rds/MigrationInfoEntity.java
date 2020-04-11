package com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity(name = "migration_info")
@EqualsAndHashCode(exclude = "candidate")
public class MigrationInfoEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "destination")
    private String destination;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private RdsCandidateEntity candidate;
}
