FROM adoptopenjdk:11-jre-hotspot
USER root
RUN apt-get update && apt-get install openssl -y
RUN </dev/null openssl s_client -connect nikoartgallery.com:443 -servername nikoartgallery.com \
    | openssl x509 > opt/java/openjdk/lib/security/nikoartgallery.cert
RUN cd opt/java/openjdk/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias nikocert -file nikoartgallery.cert
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/application.jar"]