#!/usr/bin/env bash
mysql -uroot -p -e "create database log_parser_test;"
mysql -uroot -p -e "GRANT ALL PRIVILEGES ON log_parser_test.* to 'parseruser';"
mysql -uroot -p -e "USE log_parser_test; CREATE TABLE log_record(log_date DATETIME, ip_address varchar(15), request varchar(1024), status smallint, user_agent varchar(1024));"