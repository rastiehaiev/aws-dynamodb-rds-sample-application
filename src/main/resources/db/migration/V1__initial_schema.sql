CREATE TABLE candidate
(
    id                   SERIAL PRIMARY KEY NOT NULL,
    uuid UUID NOT NULL,
    first_name           VARCHAR(255) DEFAULT NULL,
    last_name            VARCHAR(255) DEFAULT NULL,
    programming_language VARCHAR(255) DEFAULT NULL,
    migrated             BOOLEAN      DEFAULT FALSE
);

CREATE TABLE experience
(
    id              SERIAL PRIMARY KEY NOT NULL,
    company         VARCHAR(255) DEFAULT NULL,
    project         VARCHAR(255) DEFAULT NULL,
    start_timestamp BIGINT             NOT NULL,
    end_timestamp   BIGINT       DEFAULT NULL,
    candidate_id    BIGINT             NOT NULL,
    FOREIGN KEY (candidate_id) REFERENCES candidate (id)
);

CREATE INDEX IF NOT EXISTS candidate_migrated_idx ON candidate (migrated);

ALTER TABLE candidate OWNER TO "rdsuser";
ALTER TABLE experience OWNER TO "rdsuser";