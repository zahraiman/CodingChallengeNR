# Coding Challenge for Zahra Iman
-------------------------------

##The general structure of the solution is as following:

1. The blocking I/O is chosen for this solution.
2. Per every client connection request, a requestHanlder is initiated and assigned (a separate thread).
3. The thread makes calls to "Deduplicator" class in order to write unique numbers to file.
4. There is one writer that writes all the incoming digits to the log file.
5. Multiple scenarios have been considered for resiliency to data loss:
	5.1. A shutdownHook is attached to server to make sure the writer is flushed and closed before quitting and the threads are safely aborted.
	5.2. For terminate, the same procedure is going to be performed. Calling shutdown on requestHandler, Deduplicator and LogWriter to properly flush the output and exit.
7. For better performance, a boolean array is the choice for resolving the duplication issue.

This results in average of processing 2.8m numbers every 10 seconds on MacBook Pro, 3GHz Intel Core i7, 16GB ram.

##Notes:

I considered a few other designs:
1. Have requestHandler thread to just deduplicate and write to a dedicated buffer (per client). Then, have a separate thread per client to write from buffer into the file.
	1.1. This resulted in lower performance around 800,000 numbers per 10 seconds
2. Have requestHandler thread to just deduplicate and write to a global buffer (ConcurrentLinkedQueue). Then, have a separate thread (a single writer thread) to consume numbers from the global buffer and write to file.
	2.1. This resulted in lower performance around 1.7m numbers per 10 seconds


##Assumptions:

1. The purpose of this application is not to scale up to tens of thousands of clients. The main architecture is based on the fact that the number of clients is very small. If the number of clients were going to be high, the java nio could have been the better choice.
2. For better performance, a boolean array is the choice for resolving the duplication issue. Since we have 9 digits numbers, the combinations could be 000000000 to 999999999. Therefore, we could have only 1,000,000,000 number of combinations. The assumption is that we could fit a boolean array of this size (3,815 megabytes) in memory by increasing default heap size. This would remove the need for keeping some sort of database and hence improving performance. (Since Java uses an entire byte for each element in the boolean array, we will end up with using 3,815 megabytes of memory usage.)
3. There is one instance of ServerSocket only. It calls a new thread (requestHandler) for each request it receives but we have only one instance of the main server.

##Running:

1. The heapsize configuration I used is "-Xms512m -Xmx4g"
2. The shadow jars can be created by gradle build
	2.1. This will build coding-challenge-shadow.jar: This is the main file running server on "localhost" and port = 4000
3. If you want to run client, you can build the jar file for "Client" class as well
	3.1. This will build coding-challenge-client-shadow.jar: Sample client main file to run 5 client connections and generate random numbers and send to server

##===========================================================================

## Starter build framework for the coding challenge

First, you do not need to use this starter framework for your project.
If you would rather use a different build system (maven, javac, ...)
you are free to so long as you provide clear commands to build your
project and start your server.  Failure to do so will invalidate your
submission.


## Install Java

This coding challenge is in Java so it is recommended you install Java
1.8 from Oracle.


## Gradle

The build framework provided here uses gradle to build your project
and manage your dependencies.  The `gradlew` command used here will
automatically download gradle for you so you shouldn't need to install
anything other than java.


### Project Layout

All source code should be located in the `src/main/java` folder.
If you wish to write any tests (not a requirement) they should be
located in the `src/test/java` folder.

A starter `Main.java` file has been provided in the `com/newrelic/codingchallenge` package under `src/main/java`.


### Dependencies

If your project has any dependencies you can list them in the
`build.gradle` file in the `dependencies` section.


### Building your project from the command line

To build the project on Linux or MacOS run the command `./gradlew build` in a shell terminal.  This will build the source code in
`src/main/java`, run any tests in `src/test/java` and create an output
jar file in the `build/libs` folder.

To clean out any intermediate files run `./gradlew clean`.  This will
remove all files in the `build` folder.


### Running your application from the command line

You first must create a shadow jar file.  This is a file which contains your project code and all dependencies in a single jar file.  To build a shadow jar from your project run `./gradlew shadowJar`.  This will create a `codeing-challenge-shadow.jar` file in the `build/libs` directory.

You can then start your application by running the command
`java -jar ./build/lib/coding-challenge-shadow.jar`

## IDEA

You are free to use whichever editor or IDE you want providing your
projects build does not depend on that IDE.  Most of the Java
developers at New Relic use IDEA from
[JetBrains](https://www.jetbrains.com/).  JetBrains provides
a community edition of IDEA which you can download and use without
charge.

If you are planning to use IDEA you can generate the IDEA project files
by running `./gradlew idea` and directly opening the project folder
as a project in idea.

