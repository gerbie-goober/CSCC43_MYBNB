# **MYBNB USER MANUAL**
## _**RUNNING THE PROGRAM**_
> * PREREQUISITE: must have Java and MySQL installed on computer.
> * Install `en-parser-chunking.bin` from https://opennlp.sourceforge.net/models-1.5/ for data training file (that will be used for the review query).
> * Ensure that binary file is in same directory as cloned repository.
> * Clone repository in a Java supported IDE.
> * NOTE: Remember to edit the Driver.java to replace the USER and PASS variables to your user and password to your MySQL account.
> * If you do not have Java-embed SQL.jar, and the other .jar files added to your repository, please do so.
> * RECOMMENDED IDE: IntelliJ
> * After cloning, open mysql and create a table (if does not exist) cscc43_mybnb;
> * Then, run `source mybnb_load.ddl;` on MySQL client to load schemas and data.
> * Then, run Driver.java to upstart MyBnB.
