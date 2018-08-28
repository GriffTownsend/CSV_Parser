# CSV_Parser
Implementation of CSV Parser that uses basic Spring for AOP, and doesn't initialize a Spring Boot resource. 

Uses MySQL as db backend.

### Schema Used
`USE log_parser;`

`CREATE TABLE log_record(id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, log_date DATETIME, ip_address varchar(15), request varchar(1024), status smallint, user_agent varchar(1024));
`