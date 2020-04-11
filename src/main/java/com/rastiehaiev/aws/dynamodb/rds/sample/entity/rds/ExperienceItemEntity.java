package com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity(name = "experience")
@EqualsAndHashCode(exclude = "candidate")
public class ExperienceItemEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "company")
    private String company;
    @Column(name = "project")
    private String project;
    @Column(name = "start_timestamp")
    private long startTimestamp;
    @Column(name = "end_timestamp")
    private Long endTimestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private RdsCandidateEntity candidate;
}
