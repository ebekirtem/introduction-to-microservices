CREATE TABLE resource (
	id SERIAL PRIMARY KEY,
    s3_key 	varchar(255)  NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);