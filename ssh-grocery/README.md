# SSH Grocery
This repository contains a Java project folder that works with IntelliJ.

## How to use this repository
1. Following the instruction to prepare the environments.
2. You need to create your own `dev-x` branch when doing your part. Details for naming a branch is available on [Notion](https://www.notion.so/mironchen/Guidance-for-Git-14803f1709d780c284a0dedc41dda4c7?pvs=25).
3. The design for command line program is available on [Notion](https://www.notion.so/mironchen/Command-line-Design-ea6163d7f11d46b9af70279a3c331d93?pvs=25). Follow the instructions when implementing methods.
4. Make sure you create a branch for **each** individual feature or bug-fix.
5. Merge to `main` when you finish developing a feature or solve a bug.
6. DO NOT `push` or `merge` with errors. Maintain as fewer warnings as possible.

| IMPORTANT: You may need to pull the whole folder(`ssh-grocery`) instead of `SSH-General` to keep CI/CD settings up to date.

## Install and prepare environments
### Pre-requirements
- Git
- [IntelliJ IDEA Ultimate 2024.3](https://www.jetbrains.com/idea/download/)
- [Docker Desktop](https://www.docker.com)

### Install
1. First clone this repository to a secure place. Make sure you have git install on your device to do so.
   ```
    git clone https://git.cs.bham.ac.uk/rxc328/ssh-grocery.git
   ```
2. Open `ssh-grocery` folder with IntelliJ IDEA.

3. You may need to set the JDBC on IntelliJ IDEA, a driver for Java to communicate with Postgres. See https://www.jetbrains.com/help/idea/jdbc-drivers.html.


### Access online database
A PostgreSQL database has been set on cloud server. You may access it via pgAdmin 4 with the following settings.
```
host name / address : ssh-cloud.mironchen.me
port: 18418
username: ssh_cloud_admin
password: nn4z4NW9JbHATjtV
```

IMPORTANT: The postgresql is NOT yet encrypted with SSL. DO NOT share the information to anyone else.


### (Optional for local development) Set up Docker and database
1. You need to download Docker Desktop on your computer.
2. Install Postgres image in Docker. Here we specifically install postgreSQL 16.5.
   ```
	docker pull postgres:16.5
   ```

3. Create a database:
   > Make sure port 5432 is free in your host device (your laptop or computer). You may use `lsof -i:5432`(macOS & Linux) or `netstat -ano | findstr :5432`(WIndows) in your device to check.

   ```
	docker run -d \
    	-e POSTGRES_PASSWORD=password \
    	-e POSTGRES_USER=ssh_cloud_admin \
    	-p 5432:5432 \
    	--name ssh-cloud-database postgres:16.5
   ```
   The database should be setup with the command above.
   - Docker container name: `ssh-cloud-database`,
   - Postgres username: `ssh_cloud_admin`.
   - Postgres password: `password`.
   - port: `5432`
   
   Note that the database is not yet created with the above steps.

   
4. You may need to import the `init.sql` and `ssh_cloud_database.sql` file to the container before adding data from any sql file (Excl."<>" symbols).
   > You may use `docker ps` to see `container_id`.
   ```
   docker cp <local/path/to/init.sql> <container_id>:/path/to/destination/init.sql
   ```
   The `/path/to/destination/init.sql` can be `home/init.sql`.

   Create a database with `init.sql`.
   ```
   docker exec -it <container_id> psql -U ssh_cloud_admin -f <path/to/init.sql>
   ```

5. Then you will need to import `ssh_cloud_database.sql` to container as well. 
   ```
   docker cp <local/path/to/ssh_cloud_database.sql> <container_id>:/path/to/destination/ssh_cloud_database.sql
   ```

   Add data with provided `ssh_cloud_database.sql` file afterwards.
   ```
   docker exec -it <container_id> psql -U ssh_cloud_admin -d ssh_cloud_database -f <path/to/ssh_cloud_database.sql>
   ```
   Here, `postgres` is the username. 

6. (Optional) You can manage the database with [pgAdmin 4](https://www.pgadmin.org/download/).

7. (Optional) You may enable [GitHub Copilot](https://github.com/features/copilot) in IntelliJ IDEA to assist you. 
   

## File structure
1. The SSH-Server foler contains an IntelliJ project folder. JDBC and other dependencies are included. NOTE: Dependencies are still being prepared, files may change at eny time.
2. The database folder includes a PostgreSQ database, which can be accessed by server. More details about the database is available on [Notion](https://www.notion.so/mironchen/Database-Infos-1a05ebd813cb42ce89e2c324e32cd0eb?pvs=4).

## Collaborate with team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)