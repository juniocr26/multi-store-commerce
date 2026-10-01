CREATE TABLE stores (
    id UUID PRIMARY KEY,
    slug VARCHAR(80) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    name VARCHAR(160) NOT NULL CHECK (length(trim(name)) > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);
