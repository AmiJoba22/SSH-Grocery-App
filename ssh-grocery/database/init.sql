DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ssh_cloud_admin') THEN
        CREATE USER ssh_cloud_admin WITH PASSWORD 'nn4z4NW9JbHATjtV';
    END IF;
END
$$;

-- Create a new database
CREATE DATABASE ssh_cloud_database OWNER ssh_cloud_admin;

-- Connect to the newly created database
\c ssh_cloud_database;