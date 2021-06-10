# mecm-appo

#### Description
Application orchestrator is the core module responsible for orchestrating life cycle maintenance operation of application.

#### Compile and build
The Appo project is containerized based on docker, and it is divided into two steps during compilation and construction.

#### Compile
Appo is a Java program written based on jdk1.8 and maven. To compile, you only need to execute mvn install to compile and generate jar package

#### Build image
The Appo project provides a dockerfile file for mirroring. You can use the following commands when making a mirror

docker build -t edgegallery/mecm-appo:latest -f docker/Dockerfile .