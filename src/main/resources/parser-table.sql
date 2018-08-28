-- log_parser is the db name I used for sample.
USE log_parser;
CREATE TABLE log_record(id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, log_date DATETIME, ip_address varchar(15), request varchar(1024), status smallint, user_agent varchar(1024));

--command to Query for hourly counts
