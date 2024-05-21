# **[Blog-2] Enhancing Observability in Microservices with OpenTelemetry**


Welcome back to our multi-part series on enhancing observability in microservices using OpenTelemetry (Otel)! In this second installment, we'll weave the Otel Java agent into our existing Scala microservices. This integration will enable us to observe traces, metrics, and logs directly from Docker logs, providing us with deeper insights into our applications' operations. Let's get into the setup details without further ado.

### **Preparing the OpenTelemetry Java Agent**

To begin, we need the OpenTelemetry Java agent, which can be downloaded from the official [OpenTelemetry GitHub releases page](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar). This agent will automatically collect telemetry data such as traces and metrics and logs from our applications.

**Setting up the Agent:**

```bash
mkdir agents
cd agents
wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

Here, we download and store the **`opentelemetry-javaagent.jar`** in a directory named **`agents`** within your project. This setup ensures that the agent is readily available for our Docker configuration.

### **Updating the Dockerfile**

To make our Docker containers aware of the OpenTelemetry agent, we need to update our **`Dockerfile`** to include the agent in our images.

**Revised Dockerfile:**

```docker
FROM eclipse-temurin:8-jre
WORKDIR /app

# Copy the built Play Framework application
COPY my-play-application-0.1.0 /app/my-play-application-0.1.0
# Copy the Otel Java agent
COPY agents /app/agents
CMD ["/app/my-play-application-0.1.0/bin/my-play-application"]

EXPOSE 9000
```

This adjustment ensures that the Java agent is part of the container, facilitating the automatic instrumentation of our application.

### **Configuring Docker Compose**

With the agent in place, the next step involves setting the necessary environment variables to fully leverage the OpenTelemetry capabilities within our **`docker-compose.yml`**.

```yaml
services:
  service1:
    build: .
    ports:
      - "${SERVICE_ONE_PORT}:9000"
    environment:
      JAVA_OPTS: "-Xms512m -Xmx624m -javaagent:/app/agents/opentelemetry-javaagent.jar"
      OTEL_SERVICE_NAME: ${SERVICE_ONE_NAME}
      OTEL_RESOURCE_ATTRIBUTES: "service=${SERVICE_ONE_NAME},service.name=${SERVICE_ONE_NAME},env=nalpha"
      OTEL_LOGS_EXPORTER: "console"
      OTEL_METRICS_EXPORTER: "console"
      OTEL_TRACES_EXPORTER: "console"
      SERVICE_NAME: ${SERVICE_ONE_NAME}
      service1_URL: http://${SERVICE_ONE_NAME}:9000
      service2_URL: http://${SERVICE_TWO_NAME}:9000

  service2:
    build: .
    ports:
      - "${SERVICE_TWO_PORT}:9000"
    environment:
      JAVA_OPTS: "-Xms512m -Xmx624m -javaagent:/app/agents/opentelemetry-javaagent.jar"
      OTEL_SERVICE_NAME: ${SERVICE_TWO_NAME}
      OTEL_RESOURCE_ATTRIBUTES: "service=${SERVICE_TWO_NAME},service.name=${SERVICE_TWO_NAME},env=nalpha"
      OTEL_LOGS_EXPORTER: "console"
      OTEL_METRICS_EXPORTER: "console"
      OTEL_TRACES_EXPORTER: "console"
      SERVICE_NAME: ${SERVICE_TWO_NAME}
      service1_URL: http://${SERVICE_ONE_NAME}:9000
      service2_URL: http://${SERVICE_TWO_NAME}:9000
```

In our Docker Compose configuration for integrating OpenTelemetry, we've introduced several new environment variables. These are crucial for configuring how the Java agent operates within our microservices, specifying what data is collected and how it's exported. Let’s break down each of these variables for a clearer understanding of their roles and implications:

**Detailed Breakdown of Environment Variables Added to Docker Compose**

1. **JAVA_OPTS**:
    - **Purpose**: This variable is used to set Java Virtual Machine (JVM) options.
    - **Value**: **`"-Xms512m -Xmx624m -javaagent:/app/agents/opentelemetry-javaagent.jar"`**
    - **Explanation**:
        - **`Xms512m`** sets the initial heap size of the JVM to 512 megabytes.
        - **`Xmx624m`** sets the maximum heap size of the JVM to 624 megabytes, ensuring that our services have enough memory to operate efficiently but are also bounded to prevent excessive resource usage.
        - **`javaagent:/app/agents/opentelemetry-javaagent.jar`** instructs the JVM to use the OpenTelemetry Java agent, which is stored at the specified path inside the container. This agent is responsible for capturing telemetry data such as metrics, traces, and logs.
2. **OTEL_SERVICE_NAME**:
    - **Purpose**: Defines the service name for this instance in the observability tools.
    - **Value**: **`${SERVICE_ONE_NAME}`** or **`${SERVICE_TWO_NAME}`**
    - **Explanation**: This variable is used by OpenTelemetry to label the traces and metrics collected from the service, making it easier to identify data in the monitoring tools. Each service has its own distinct name which aids in disaggregating the data collected across different services.
3. **OTEL_RESOURCE_ATTRIBUTES**:
    - **Purpose**: Provides additional attributes for the service that will be attached to all telemetry data.
    - **Value**: **`"service=${SERVICE_ONE_NAME},service.name=${SERVICE_ONE_NAME},env=nalpha"`**
    - **Explanation**: This setting allows you to define custom attributes that describe the deployed environment and other metadata:
        - **`service`** and **`service.name`** are often set to the same value and are used to describe the logical name of the service in trace data.
        - **`env=nalpha`** specifies the environment in which the service is running, which can be crucial for filtering data during analysis in a multi-environment setup.
4. **OTEL_LOGS_EXPORTER**, **OTEL_METRICS_EXPORTER**, **OTEL_TRACES_EXPORTER**:
    - **Purpose**: Defines the exporters to be used for logs, metrics, and traces respectively.
    - **Value**: **`"console"`**
    - **Explanation**: These variables specify that the telemetry data (logs, metrics, and traces) should be exported to the console. This setup is particularly useful for development and debugging purposes, where you can view the outputs directly in the logs of the container.
    
    You can read more about **Otel parameters [here](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration)** 
    
    ### **Deploying the new setup**
    
    With our configurations in place, it’s time to relaunch our services:
    
    ```bash
    docker-compose -f docker-compose.yml down && docker-compose -f docker-compose.yml up -d --build --remove-orphans
    ```
    
    This command sequence ensures that our changes are applied by rebuilding the Docker images and restarting the containers.
    
    ### **Observing the Outputs**
    
    Once the services are up, you can observe the logs to see the OpenTelemetry outputs:
    
    ```bash
    docker logs -f service1
    docker logs -f service2
    ```
    
    These logs will now include detailed telemetry data, helping us understand the internal state and interactions of our microservices in real-time.
    
    ### Understanding the telemetry data
    
    ### **Traces**
    
    **Definition**: A trace represents the full lifecycle, or a significant segment, of a single request as it moves through the services that make up an application. A trace is made up of spans, where each span represents a discrete unit of work done in the system.
    
    **Example Use Case**: Consider a user request to check out an item from an e-commerce platform. This operation might involve several services such as the shopping cart service, payment service, and shipping service. A trace for this request would provide a detailed path of the request across these services, highlighting any delays or failures in the process.
    
    **From the Logs**:
    
    ```prolog
    [otel.javaagent 2024-05-21 14:54:04:327 +0000] [application-akka.actor.default-dispatcher-8] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'GET /' : f49250f59923dc0a13f1d2d6553fa577 aac2ddea5100baf3 SERVER [tracer: io.opentelemetry.akka-http-10.0:2.4.0-alpha] AttributesMap{...}
    ```
    
    This trace entry shows a **`GET /`** request being processed, with detailed attributes that could help identify issues like slow response times or errors within specific services handling the request.
    
    ### **Metrics**
    
    **Definition**: Metrics are quantifiable measurements that are collected continuously over time. They provide statistical data that can be analyzed to understand the behaviour of the system.
    
    **Example Use Case**: Monitoring the CPU and memory usage of your service can help identify when resource utilization approaches critical limits. By setting alerts on these metrics, you can proactively scale your infrastructure or optimize resource usage before users experience slowdowns or outages.
    
    **From the Logs**:
    
    ```prolog
    [otel.javaagent 2024-05-21 14:53:48:395 +0000] [PeriodicMetricReader-1] INFO io.opentelemetry.exporter.logging.LoggingMetricExporter - metric: ImmutableMetricData{... name=jvm.memory.committed, description=Measure of memory committed., unit=By, type=LONG_SUM, data=...}
    ```
    
    This metric shows the amount of memory committed by the JVM, which is vital for understanding if the service is running close to its memory limit, potentially leading to performance degradation or crashes.
    
    ### **Logs**
    
    **Definition**: Logs are textual records of events that have occurred within an application. They can include a wide range of information from debug messages to critical errors.
    
    **Example Use Case**: When an error occurs, such as a payment failure in an e-commerce application, logs can provide immediate contextual information about the error, such as the time it occurred and specific error messages returned by the payment gateway.
    
    **From the Logs**:
    
    ```prolog
    2024-05-21 14:54:04,240 [INFO] from controllers.SampleController in application-akka.actor.default-dispatcher-8 - Service: service2, Time: 2024-05-21T14:54:04.239
    ```
    
    This log entry provides a timestamped record of an operation within the service, useful for pinpointing when specific actions were taken or when errors were logged, facilitating easier troubleshooting.
    
    ### **Integrating Traces, Metrics, and Logs**
    
    The integration of traces, metrics, and logs provides a comprehensive observability solution. For instance, if an application experiences a sudden slowdown:
    
    1. **Metrics** might show a spike in CPU usage or memory consumption.
    2. **Traces** could reveal that a particular service is taking longer than usual to respond.
    3. **Logs** could provide error messages or warnings that indicate exceptions thrown or resource constraints encountered during this period.
    
    Together, these data points allow developers and system operators to quickly diagnose and resolve issues, ensuring application reliability and performance. This holistic approach to data collection and analysis is crucial in complex, distributed systems where pinpointing the source of problems can otherwise be very challenging.
    
    ### **Conclusion**
    
    As we wrap up this blog on enhancing observability in microservices with OpenTelemetry, we've taken a looked into the integration of the OpenTelemetry Java agent within our Scala microservices. We've explored how to configure and utilize traces, metrics, and logs to gain comprehensive insights into our application's operations. These tools are not just about monitoring; they are about understanding the finer workings of our services, pinpointing inefficiencies, diagnosing issues, and ultimately ensuring that our systems are robust and performant.
    
    Looking ahead to the next blog in the series, we'll expand our observability toolkit by integrating Grafana, Prometheus, Loki, and Tempo. These powerful tools will allow us to visualize the telemetry data we're collecting in more intuitive ways, set up alerts based on specific metrics, analyze logs more effectively, and trace transactions end-to-end in a more user-friendly manner. This will not only enhance our ability to monitor our systems in real-time but also enable us to react more swiftly and effectively to any anomalies or issues that arise.