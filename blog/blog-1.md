# **[Blog 1] Orchestrating Microservices with Docker Compose: A Scala and Play Framework Example**


In this blog, we'll delve into setting up a microservices architecture using Scala, the Play Framework, and Docker. We'll outline how two microservices can interact seamlessly over HTTP, facilitated by Docker Compose, providing a practical guide for both novice and experienced DevOps engineers.

### **Overview of the Project Structure**

The project leverages several components to manage the build and deployment processes efficiently:

- **build.sbt**: Configures the Scala build environment.
- **Scala Controllers**: Handle application logic and HTTP request routing.
- **Dockerfile**: Specifies the runtime environment and packages the application.
- **docker-compose.yml**: Manages container orchestration.
- **.env file**: Centralizes configuration by storing environment variables.

### **Configuration and Setup**

1. **Build Configuration (`build.sbt`)**:
    - The **`build.sbt`** file is pivotal in setting up project metadata, managing dependencies, and defining the Scala version. It ensures that all necessary libraries, such as Play Framework components and logging utilities, are available for the application.
2. **Scala Play Framework Controllers (`ServiceAController.scala`)**:
    - **Root Route (`GET /`)**: Responds to HTTP GET requests at the root URL. It utilizes a **`logLevel`** query parameter to demonstrate dynamic logging capabilities.
    - **Inter-Service Communication (`GET /other/:serviceName`)**: Facilitates communication between services using asynchronous HTTP calls, highlighting the microservices' ability to interact through the network.
    - **Resource Management Routes (`GET /cause-gc` and `GET /cause-oom`)**: These endpoints are instrumental for testing the application's behavior under specific conditions like triggering garbage collection or simulating memory overflow.

### **Local Environment Setup**

The github repo contains the package at  my-play-application-0.1.0
To package the application locally:

```bash
sbt clean compile dist
unzip target/universal/my-play-application-0.1.0.zip
```

These commands prepare the application for deployment by compiling the Scala files, packaging the application into a distributable format, and extracting the distribution.

### **Docker Environment Configuration**

The **`Dockerfile`** sets up the Java Runtime Environment, copies the Play application into the Docker container, and exposes port 9000, preparing the application for network communication.

```docker
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY my-play-application-0.1.0 /app/my-play-application-0.1.0
CMD ["/app/my-play-application-0.1.0/bin/my-play-application"]
EXPOSE 9000
```

### **Docker Compose for Microservices**

Docker Compose is crucial for defining and running multi-container Docker applications. It simplifies the configuration of services, networks, and volumes, and enables the setup of an entire application with a single command.

**Structure of `docker-compose.yml`**:

```yaml
services:
  service1:
    build: .
    ports:
      - "${SERVICE_ONE_PORT}:9000"
    environment:
      SERVICE_NAME: ${SERVICE_ONE_NAME}
      service1_URL: http://${SERVICE_ONE_NAME}:9000
      service2_URL: http://${SERVICE_TWO_NAME}:9000
  service2:
    build: .
    ports:
      - "${SERVICE_TWO_PORT}:9000"
    environment:
      SERVICE_NAME: ${SERVICE_TWO_NAME}
      service1_URL: http://${SERVICE_ONE_NAME}:9000
      service2_URL: http://${SERVICE_TWO_NAME}:9000

```

Each service is defined with build context, port mappings, and specific environment variables.

**Key Components**

- **Services**: Defines the different containers that make up your application. Here **`service1`** and **`service2`** are two separate services.

Each service section can contain the following:

- **build**: Specifies the directory containing the Dockerfile and the context for building the Docker image.
- **ports**: Maps the container’s ports to the host. Format is **`HOST:CONTAINER`**.
- **environment**: Defines environment variables inside the container.

**Understanding Environment Variables and the `.env` File**

Docker Compose utilizes an **`.env`** file to manage environment variables that configure services dynamically:

**Example `.env` file**:

```bash
SERVICE_ONE_NAME=service1
SERVICE_ONE_PORT=9001
SERVICE_TWO_NAME=service2
SERVICE_TWO_PORT=9002
```

These variables are referenced in **`docker-compose.yml`** using **`${VARIABLE_NAME}`** syntax, allowing Docker Compose to replace placeholders with actual values at runtime, which is instrumental for adjusting settings without altering the docker-compose file.

### **Networking and Service Interaction**

The containers, **`service1`** and **`service2`**, communicate over a Docker-managed network, using ports 9000 internally. So inside the backend services will connect with each other at http://<service name>:9000 which in our case is http://service1:9000 and http://service2:9000  Externally, these services are accessible on the host machine via ports 9001 and 9002 as configured in the ports config, respectively. This setup allows for seamless interaction both internally among services and externally for debugging and API access.

### **Running and Testing the Microservices with Docker Compose**

Once your Docker environment is set up, you can easily bring up the microservices and test their interactions using the following steps:

**Bringing Up the Containers**

To start the microservices, navigate to the directory containing your **`docker-compose.yml`** file and run:

```bash
docker-compose -f docker-compose.yml up -d
```

This command launches the services defined in **`docker-compose.yml`** in detached mode (**-d**), meaning they run in the background. This setup allows the microservices to communicate with each other as configured without blocking access to the command line.

### **Checking container and container logs**

An essential aspect of managing microservices is the ability to monitor and debug them using logs generated during their operation. Docker provides a straightforward method to view these logs, helping you track down issues, understand service interactions, and monitor system behavior in real-time.

**Checking Active Containers**

Before viewing the logs, you need to identify the containers for which you want to see the logs. Use the following command to list all active containers:

```bash
docker ps
```

This command displays a list of all running containers along with their details like CONTAINER ID, IMAGE, COMMAND, CREATED status, PORTS, and NAMES. Here’s what the output might look like:

```bash
CONTAINER ID   IMAGE                         COMMAND                  CREATED       STATUS       PORTS                                       NAMES
b3399a11f27f   play-observability-service2   "/__cacert_entrypoin…"   2 hours ago   Up 2 hours   0.0.0.0:9002->9000/tcp, :::9002->9000/tcp   play-observability-service2-1
e50c71c0e9f6   play-observability-service1   "/__cacert_entrypoin…"   2 hours ago   Up 2 hours   0.0.0.0:9001->9000/tcp, :::9001->9000/tcp   play-observability-service1-1
```

**Viewing Logs**

Once you have identified the container names or IDs from the **`docker ps`** output, you can view the logs using the **`docker logs`** command. This is particularly useful for troubleshooting and ensuring that your microservices are functioning as expected.

For **`service1`**, use:

```bash
docker logs -f play-observability-service1-1
```

For **`service2`**, use:

```bash
docker logs -f play-observability-service2-1
```

The **`-f`** flag "follows" the log output, meaning you can see log entries in real-time as they are being written. This is equivalent to "tailing" the logs.

**Sample Log Outputs**

Here are some snippets from what you might see in the logs for each service:

- **service1 Logs**:
    
    ```prolog
    2024-05-21 10:20:21,376 [INFO] from play.api.Play in main - Application started (Prod)
    2024-05-21 11:49:00,838 [ERROR] from controllers.SampleController - Service: service1, Time: 2024-05-21T11:49:00.837
    ```
    
- **service2 Logs**:
    
    ```prolog
    2024-05-21 10:20:18,112 [INFO] from akka.event.slf4j.Slf4jLogger - Slf4jLogger started
    2024-05-21 11:49:10,883 [ERROR] from controllers.SampleController - Service: service2, Time: 2024-05-21T11:49:10.883
    ```
    

### **Testing Setup with CURL**

With the services running, you can now test their functionality and interaction using CURL. Here's how you can verify each service individually and their ability to communicate with each other:

**Test Service 1** :

- Send a request to **`service1`** with the log level set to **`error`**:
    
    ```bash
    curl "http://localhost:9001/?logLevel=error"
    
    Service: service1, Time: 2024-05-21T11:49:00.837%
    ```
    

**Test Service 2** :

- Send a request to **`service2`** with the log level set to **`error`**:
    
    ```bash
    curl "http://localhost:9002/?logLevel=error"
    
    Service: service2, Time: 2024-05-21T11:49:05.974%
    ```
    

**Inter-Service Communication from Service 1 to Service 2**:

- Trigger **`service1`** to call **`service2`**, setting the log level to **`error`**:
    
    ```bash
    curl "http://localhost:9001/other/service2?logLevel=error"
    
    Service: service2, Time: 2024-05-21T11:49:10.883%
    ```
    

**Inter-Service Communication from Service 2 to Service 1**:

- Trigger **`service2`** to call **`service1`**, setting the log level to **`error`**:
    
    ```bash
    curl "http://localhost:9002/other/service1?logLevel=error"
    
    Service: service1, Time: 2024-05-21T11:49:16.106%
    ```
    

By following these steps, you ensure that your services are not only operational but also interact correctly, providing a dependable and scalable service architecture ready for further development and deployment.

### **Conclusion**

This blog post marks the beginning of a multi-blog series on the observability of a sample Java application using OpenTelemetry over Docker Compose. We started by setting up Scala microservices over the Play Framework and Docker Compose, laying the groundwork for deeper insights into observability practices in subsequent posts.
