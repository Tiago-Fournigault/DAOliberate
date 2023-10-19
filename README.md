# DAOliberate

Decentrally Moderated Chat through Collaborative Decision Making

2022-2023, 2nd semester


## Author

**Group A25**

92562 [Tiago Fournigault](mailto:tiago.fournigault@tecnico.ulisboa.pt)


## About the project

The deliberation of ideas in organizations and communities is a very important and desired activity. Decentralized autonomous organizations (DAOs) give even greater importance to this process, as they are organizations without central leadership, and need deliberation processes to define the future steps to follow. However, in most organizations and communities, deliberation is carried out in chats centrally moderated by figures who hold all the power of moderation. This can discourage discussion of subjects that go against the moderators' ideas due to fear of negative consequences, such as being censored or banned from the chat. To resolve this issue, we introduce DAOliberate, a decentrally moderated chat that aims to promote deliberation and remove fears during the discussion of ideas. DAOliberate allows moderation tasks to be performed on behalf of the entire community through a collaborative decision making process. This process is carried out through a voting mechanism combined with a reputation system, which allows to determine the most trusted members of the community.


## Getting Started

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.
However, if you want to run the system in a docker container you only need to have docker installed.

To confirm that you have them installed, open a terminal and type:

```
javac --version

mvn --version

docker --version
```

### Using docker
### Run in docker container

First create a docker image by running the following command in the folder where the *Dockerfile* is located:

```sh
$ docker build -t daoliberate .
```

Then create a container that runs the previously created image:

```sh
$ docker run -it --name daoliberate daoliberate
```

The creation of the container will launch the servers in the background, along with the client application. The client application allows interaction with the system and will wait for input from the user.

If you wish to launch more client applications to interact with the system, simply execute the following commands in another terminal:

```sh
$ docker exec -it daoliberate /bin/bash
$ mvn exec:java
```

### Without using docker
### Installing

To compile and install all modules:

```sh
$ mvn clean install -DskipTests
```

The integration tests are skipped because they require theservers to be running.

### Running the project

#### Create digital certificates

To create the digital certificates needed to run the servers just go to the *cert* folder and run the following command:

```sh
$ ./gen.sh
```

#### Launching the Daoliberate server

To launch the Daoliberate server just go to the *server* folder and execute the following command:

```sh
$ mvn exec:java
```

#### Launching the Register server

To launch the Register server just go to the *register* folder and execute the following command:

```sh
$ mvn exec:java
```

#### Launching the Client application

To launch the Client application just go to the *client* folder and execute the following command:

```sh
$ mvn exec:java
```

### Run the tests

To run the tests just go to the *tester* folder and execute the following command:

```sh
$ mvn -Dtest=<name of class with tests> test
```

The classes with tests are in the folder *tester/src/test/java/pt/tecnico/grpc/tester*.

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework

----

[System Admin](mailto:tiago.fournigault@tecnico.ulisboa.pt)
