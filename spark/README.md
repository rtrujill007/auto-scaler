# Plane Streaming Application
Sample Application using Spark/Spark Streaming Read from Kafka

## Instructions

### Running the sample simulator in IntelliJ
1. Setup this project in IntelliJ 
2. Run the `Simulator` class located in the `test` directory
3. Run the `PlaneStreamingAppTester` tester located in the `test` directory.
4. Done

## Building and Running for DC/OS
1. Run `mvn clean install` to create the docker image: `rtrujill007/plane-streaming-app` (if you want to change the name edit the **pom.xml**)
2. Push the docker image to docker hub: `docker push rtrujill007/plane-streaming-app:latest`
3. Copy the [marathon.json](marathon.json)  file, configure the application to use your cluster.
4. Deploy the **plane-streaming-app** app as a marathon app (via cmd line or UI), use your modified [marathon.json](marathon.json)
5. Done

#### References

