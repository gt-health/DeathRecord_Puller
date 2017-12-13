FROM tomcat:latest
MAINTAINER Mike Riley "michael.riley@gtri.gatech.edu"

RUN apt-get update -y && apt-get upgrade -y

RUN apt-get install -y \
      git \
	  java \
	  mvn
	  
# Define environment variable
ENV POSTGRES_USER ecrUser
ENV POSTGRES_PASSWORD ecrUserPassword
ENV POSTGRES_DB ecrdb


RUN git clone https://mriley7@git2.icl.gtri.org/scm/cdcsti/ecr_javalib.git

RUN mvn clean install -f ./ecr_javalib/pom.xml

COPY target/FHIR_Controller-0.0.1-SNAPSHOT.war $CATALINA_BASE/webapps/