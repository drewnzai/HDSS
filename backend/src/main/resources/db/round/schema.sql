-- Round database schema. Idempotent, safe to run on every start.
-- Ids are NOT auto-generated: rows keep the id they have in live, because
-- Android serverIds and the formId/questionId values in pushes refer to
-- live ids. round_number is the round a row was last moved in; moved_at is
-- when, and is what Android's pull filters on.

CREATE TABLE IF NOT EXISTS rounds (
                                      round_number INT         NOT NULL,
                                      created_at   DATETIME(6) NOT NULL,
    created_by   BIGINT      NOT NULL,
    summary      TEXT,
    PRIMARY KEY (round_number)
    );

-- Identifying columns only. No password, email or role.
CREATE TABLE IF NOT EXISTS users (
                                     id         BIGINT       NOT NULL,
                                     first_name VARCHAR(255),
    last_name  VARCHAR(255),
    username   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS locations (
                                         id            BIGINT       NOT NULL,
                                         name          VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    parent_id     BIGINT,
    ancestor_path VARCHAR(255) NOT NULL,
    code          VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_locations_parent (parent_id),
    KEY idx_locations_ancestor_path (ancestor_path),
    CONSTRAINT fk_locations_parent FOREIGN KEY (parent_id) REFERENCES locations (id)
    );

CREATE TABLE IF NOT EXISTS forms (
                                     id           BIGINT       NOT NULL,
                                     name         VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    category     VARCHAR(255) NOT NULL,
    target       VARCHAR(255) NOT NULL,
    version      INT          NOT NULL,
    description  VARCHAR(255),
    active       TINYINT(1)   NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    round_number INT          NOT NULL,
    moved_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_forms_name (name)
    );

CREATE TABLE IF NOT EXISTS questions (
                                         id                 BIGINT       NOT NULL,
                                         form_id            BIGINT       NOT NULL,
                                         name               VARCHAR(255) NOT NULL,
    label              VARCHAR(255) NOT NULL,
    hint               VARCHAR(255),
    type               VARCHAR(255) NOT NULL,
    required           TINYINT(1)   NOT NULL,
    relevant           VARCHAR(255),
    constraint_value   VARCHAR(255),
    constraint_message VARCHAR(255),
    calculation        VARCHAR(255),
    choice_list_name   VARCHAR(255),
    order_index        INT          NOT NULL,
    mapped_entity      VARCHAR(255) NOT NULL,
    mapped_field       VARCHAR(255),
    round_number       INT          NOT NULL,
    moved_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_questions_form (form_id),
    CONSTRAINT fk_questions_form FOREIGN KEY (form_id) REFERENCES forms (id)
    );

CREATE TABLE IF NOT EXISTS individuals (
                                           id            BIGINT       NOT NULL,
                                           client_id     VARCHAR(255) NOT NULL,
    extended_id   VARCHAR(255),
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    sex           VARCHAR(255) NOT NULL,
    date_of_birth DATE         NOT NULL,
    dob_estimated TINYINT(1)   NOT NULL,
    mother_id     BIGINT,
    father_id     BIGINT,
    created_by    BIGINT       NOT NULL,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    round_number  INT          NOT NULL,
    moved_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_individuals_client_id (client_id),
    KEY idx_individuals_moved_at (moved_at),
    CONSTRAINT fk_individuals_mother FOREIGN KEY (mother_id) REFERENCES individuals (id),
    CONSTRAINT fk_individuals_father FOREIGN KEY (father_id) REFERENCES individuals (id),
    CONSTRAINT fk_individuals_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS households (
                                          id             BIGINT       NOT NULL,
                                          client_id      VARCHAR(255) NOT NULL,
    household_code VARCHAR(255) NOT NULL,
    location_id    BIGINT       NOT NULL,
    latitude       DOUBLE,
    longitude      DOUBLE,
    head_id        BIGINT,
    status         VARCHAR(255) NOT NULL,
    created_by     BIGINT       NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    round_number   INT          NOT NULL,
    moved_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_households_client_id (client_id),
    UNIQUE KEY uk_households_code (household_code),
    UNIQUE KEY uk_households_head (head_id),
    KEY idx_households_location (location_id),
    KEY idx_households_moved_at (moved_at),
    CONSTRAINT fk_households_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_households_head FOREIGN KEY (head_id) REFERENCES individuals (id),
    CONSTRAINT fk_households_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS memberships (
                                           id                   BIGINT       NOT NULL,
                                           client_id            VARCHAR(255) NOT NULL,
    individual_id        BIGINT       NOT NULL,
    household_id         BIGINT       NOT NULL,
    relationship_to_head VARCHAR(255) NOT NULL,
    start_date           DATE         NOT NULL,
    start_type           VARCHAR(255) NOT NULL,
    end_date             DATE,
    end_type             VARCHAR(255),
    created_by           BIGINT       NOT NULL,
    created_at           DATETIME(6),
    updated_at           DATETIME(6),
    round_number         INT          NOT NULL,
    moved_at             DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_memberships_client_id (client_id),
    KEY idx_memberships_individual (individual_id),
    KEY idx_memberships_household (household_id),
    KEY idx_memberships_moved_at (moved_at),
    CONSTRAINT fk_memberships_individual FOREIGN KEY (individual_id) REFERENCES individuals (id),
    CONSTRAINT fk_memberships_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_memberships_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS visits (
                                      id            BIGINT       NOT NULL,
                                      client_id     VARCHAR(255) NOT NULL,
    household_id  BIGINT,
    individual_id BIGINT,
    visit_date    DATETIME(6)  NOT NULL,
    status        VARCHAR(255) NOT NULL,
    created_by    BIGINT       NOT NULL,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    round_number  INT          NOT NULL,
    moved_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_visits_client_id (client_id),
    KEY idx_visits_household (household_id),
    KEY idx_visits_individual (individual_id),
    KEY idx_visits_moved_at (moved_at),
    CONSTRAINT fk_visits_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_visits_individual FOREIGN KEY (individual_id) REFERENCES individuals (id),
    CONSTRAINT fk_visits_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );

-- started_at / completed_at are nullable until it is confirmed whether the
-- live entity really has them. Remove them if it does not.
CREATE TABLE IF NOT EXISTS form_responses (
                                              id           BIGINT       NOT NULL,
                                              client_id    VARCHAR(255) NOT NULL,
    visit_id     BIGINT       NOT NULL,
    form_id      BIGINT       NOT NULL,
    form_version INT          NOT NULL,
    status       VARCHAR(255) NOT NULL,
    started_at   DATETIME(6),
    completed_at DATETIME(6),
    created_by   BIGINT       NOT NULL,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    round_number INT          NOT NULL,
    moved_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_form_responses_client_id (client_id),
    KEY idx_form_responses_visit (visit_id),
    KEY idx_form_responses_form (form_id),
    KEY idx_form_responses_moved_at (moved_at),
    CONSTRAINT fk_form_responses_visit FOREIGN KEY (visit_id) REFERENCES visits (id),
    CONSTRAINT fk_form_responses_form FOREIGN KEY (form_id) REFERENCES forms (id),
    CONSTRAINT fk_form_responses_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS answers (
                                       id               BIGINT       NOT NULL,
                                       client_id        VARCHAR(255) NOT NULL,
    form_response_id BIGINT       NOT NULL,
    question_id      BIGINT       NOT NULL,
    value            TEXT,
    created_by       BIGINT       NOT NULL,
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    round_number     INT          NOT NULL,
    moved_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_answers_client_id (client_id),
    KEY idx_answers_form_response (form_response_id),
    KEY idx_answers_question (question_id),
    KEY idx_answers_moved_at (moved_at),
    CONSTRAINT fk_answers_form_response FOREIGN KEY (form_response_id) REFERENCES form_responses (id),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT fk_answers_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );