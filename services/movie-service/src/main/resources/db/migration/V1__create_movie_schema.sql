CREATE TABLE movies (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    title            VARCHAR(500) NOT NULL,
    original_title   VARCHAR(500),
    description      TEXT,
    duration_minutes INT          NOT NULL,
    rating           VARCHAR(10),
    language         VARCHAR(50),
    release_date     DATE,
    end_date         DATE,
    poster_url       VARCHAR(500),
    backdrop_url     VARCHAR(500),
    trailer_url      VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'COMING_SOON',
    director         VARCHAR(200),
    cast_list        TEXT,
    age_rating       VARCHAR(10),
    imdb_score       NUMERIC(3,1),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT movies_pkey PRIMARY KEY (id),
    CONSTRAINT movies_status_check CHECK (status IN ('COMING_SOON','NOW_SHOWING','ENDED','CANCELLED'))
);

CREATE TABLE movie_genres (
    movie_id UUID   NOT NULL REFERENCES movies(id),
    genre    VARCHAR(50) NOT NULL
);

CREATE TABLE movie_formats (
    movie_id UUID   NOT NULL REFERENCES movies(id),
    format   VARCHAR(20) NOT NULL
);

CREATE INDEX idx_movies_status        ON movies (status, release_date DESC);
CREATE INDEX idx_movies_title_search  ON movies USING gin (to_tsvector('simple', title));
CREATE INDEX idx_movie_genres_movie   ON movie_genres (movie_id);
