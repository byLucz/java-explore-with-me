DROP TABLE IF EXISTS users, categories, location, events, requests, compilations, compilations_events, comments;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name  VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS location
(
    id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation         VARCHAR,
    category_id        BIGINT REFERENCES categories (id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_on         TIMESTAMP NOT NULL,
    description        VARCHAR NOT NULL,
    event_date         TIMESTAMP NOT NULL,
    initiator_id       BIGINT REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    location_id        BIGINT REFERENCES location (id) ON DELETE CASCADE ON UPDATE CASCADE,
    paid               BOOLEAN NOT NULL,
    participant_limit  INTEGER NOT NULL,
    published_on       TIMESTAMP,
    request_moderation BOOLEAN NOT NULL,
    state              VARCHAR NOT NULL,
    title              VARCHAR NOT NULL,
    confirmed_requests INTEGER
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created      TIMESTAMP NOT NULL,
    event_id     BIGINT REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    requester_id BIGINT REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    status       VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title  VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS compilations_events
(
    compilation_id BIGINT REFERENCES compilations (id),
    event_id       BIGINT REFERENCES events (id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    created TIMESTAMP NOT NULL,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
