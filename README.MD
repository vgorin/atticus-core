# Atticus Core

## Installation Instructions

### Prerequisites
1. Java Runtime Environment 1.8  
  https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
2. MySQL Database 5.7  
  https://dev.mysql.com/downloads/mysql/5.7.html

### Database Setup
1. Create new database named ```atticus```:  
    ```
    mysql> create database atticus;
    Query OK, 1 row affected (0.07 sec)
    ```
2. Create new user ```atticus```:  
    ```
    mysql> create user 'atticus'@'localhost' identified by 'Atticus';
    Query OK, 0 rows affected (0.18 sec)
    
    ```
    We use password ```Atticus``` in the example above
3. Configure permissions for newly created user ```atticus``` to access newly created database ```atticus```:  
    ```
    mysql> grant all privileges on atticus.* to 'atticus'@'localhost';
    Query OK, 0 rows affected (0.01 sec)
    
    mysql> flush privileges;
    Query OK, 0 rows affected (0.11 sec)
    ```
4. Test the connection: ```mysql -u atticus -p```  
  Type your password when prompted:  
    ```
    Enter password:
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    Your MySQL connection id is 394
    Server version: 5.7.22 Homebrew
    
    Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
    
    Oracle is a registered trademark of Oracle Corporation and/or its
    affiliates. Other names may be trademarks of their respective
    owners.
    
    Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
    
    mysql> show databases;
    +--------------------+
    | Database           |
    +--------------------+
    | information_schema |
    | atticus            |
    +--------------------+
    2 rows in set (0.01 sec)
    ```

### Running the Application
1. Download project source code from [https://github.com/AtticusDotOne/Atticus-Core](https://github.com/AtticusDotOne/Atticus-Core):  
    ```
    git pull https://github.com/AtticusDotOne/Atticus-Core.git
    ```
2. Navigate into project folder and run:  
    ```
    cd Atticus-Core
    ./mvnw spring-boot:run
    ```
3. Press ```Ctrl+C``` to stop and exit application
