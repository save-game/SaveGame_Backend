FROM openjdk:11
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java","-DSpring.profiles.active=prod","-jar","/app.jar"]