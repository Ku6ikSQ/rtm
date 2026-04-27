-- =============================================
-- EXTENSIONS
-- =============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- =============================================
-- ENUMS
-- =============================================

CREATE TYPE user_role AS ENUM ('USER', 'MODERATOR', 'ADMIN');
CREATE TYPE artist_role AS ENUM ('MAIN', 'FEATURED', 'PRODUCER');


-- =============================================
-- USERS
-- =============================================

CREATE TABLE users
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username      VARCHAR(64)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          user_role    NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    image_key     VARCHAR(512)
);


-- =============================================
-- ARTISTS
-- =============================================

CREATE TABLE artists
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    stage_name VARCHAR(255) NOT NULL,
    real_name  VARCHAR(255),
    bio        TEXT,
    country    VARCHAR(100),
    image_key  VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =============================================
-- GENRES
-- =============================================

CREATE TABLE genres
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id   UUID REFERENCES genres (id) ON DELETE SET NULL
);


-- =============================================
-- PLATFORMS
-- =============================================

CREATE TABLE platforms
(
    id       UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    name     VARCHAR(100) NOT NULL,
    logo_key VARCHAR(512)
);


-- =============================================
-- ALBUMS
-- =============================================

CREATE TABLE albums
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    release_year SMALLINT     NOT NULL,
    cover_key    VARCHAR(512),
    avg_rating   NUMERIC(4, 2)         DEFAULT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT
);


-- =============================================
-- ALBUM_ARTISTS
-- =============================================

CREATE TABLE album_artists
(
    album_id  UUID        NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    artist_id UUID        NOT NULL REFERENCES artists (id) ON DELETE RESTRICT,
    role      artist_role,
    "order"   SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (album_id, artist_id)
);


-- =============================================
-- ALBUM_GENRES
-- =============================================

CREATE TABLE album_genres
(
    album_id UUID NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres (id) ON DELETE RESTRICT,
    PRIMARY KEY (album_id, genre_id)
);


-- =============================================
-- ALBUM_LINKS
-- =============================================

CREATE TABLE album_links
(
    album_id    UUID         NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    platform_id UUID         NOT NULL REFERENCES platforms (id) ON DELETE RESTRICT,
    url         VARCHAR(512) NOT NULL,
    PRIMARY KEY (album_id, platform_id)
);


-- =============================================
-- TRACKS
-- =============================================

CREATE TABLE tracks
(
    id               UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    title            VARCHAR(255) NOT NULL,
    album_id         UUID         NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    track_number     SMALLINT     NOT NULL,
    duration_seconds INTEGER      NOT NULL,
    UNIQUE (album_id, track_number)
);


-- =============================================
-- REVIEWS
-- =============================================

CREATE TABLE reviews
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    album_id    UUID        NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    author_id   UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    score       SMALLINT    NOT NULL CHECK (score BETWEEN 1 AND 10),
    review_text TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (album_id, author_id)
);