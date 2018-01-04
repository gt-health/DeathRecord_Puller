FROM tomcat:latest
MAINTAINER Mike Riley "michael.riley@gtri.gatech.edu"

RUN apt-get update -y && apt-get upgrade -y

RUN apt-get install -y \
      git \
      postgresql \
      openjdk-8-jdk \
      maven
	  
# Define environment variable
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD postgres
ENV POSTGRES_DB ecrdb

ADD . /usr/src/fhir_src
RUN mvn clean install -DskipTests -f /usr/src/fhir_src/ecr_javalib
RUN mvn clean install -DskipTests -f /usr/src/fhir_src/
COPY target/FHIR_Controller-0.0.1-SNAPSHOT.war $CATALINA_BASE/webapps/
COPY wait-for-postgres.sh /usr/local/bin/wait-for-postgres.sh
