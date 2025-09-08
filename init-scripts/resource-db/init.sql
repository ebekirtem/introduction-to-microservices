CREATE TABLE resource (
	id SERIAL PRIMARY KEY,
	content oid NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);