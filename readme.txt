
BUILD:
mvn -f spring-of-emails/pom.xml clean verify

RUN:
docker-compose up --build --scale spring-of-emails=5
