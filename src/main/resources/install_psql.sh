sudo apt update
sudo apt install postgresql postgresql-client
sudo systemctl status postgresql
sudo -i -u postgres
psql
CREATE USER user_timetracker WITH PASSWORD '000000';
CREATE DATABASE db_timetracker OWNER user_timetracker;
GRANT ALL PRIVILEGES ON DATABASE db_timetracker TO user_timetracker;
CREATE TYPE user_role_enum AS ENUM ('ADMIN', 'USER');