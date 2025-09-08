CREATE TABLE song_metadata (
	id INT PRIMARY KEY,
	name varchar(100) NOT NULL,
	album varchar(100) NOT NULL,
	artist varchar(100) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	duration varchar(10) NOT NULL,
	year varchar(255) NOT NULL
);