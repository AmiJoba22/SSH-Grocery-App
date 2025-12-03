package org.example;

public class Credentials {
    //JDBC connection
    public static final String USERNAME = "ssh_cloud_admin";
    public static final String PASSWORD = "nn4z4NW9JbHATjtV";
    public static final String URL = "jdbc:postgresql://ssh-cloud.mironchen.me:18418/ssh_cloud_database_dev";
    //Client-server connection
    public static final String HOST = "127.0.0.1"; //localhost
    public static final int PORT = 9994; //This is NOT the port in postgres, but the port at which the server is listening
    // if port is already in use, try another port
}
