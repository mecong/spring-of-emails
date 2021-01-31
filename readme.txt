
BUILD:
mvn -f spring-of-emails/pom.xml clean verify

RUN:
docker-compose up --build --scale spring-of-emails=5


USE:
POST
http://localhost:80/SpringOfEmails/feed

GET
http://localhost:80/SpringOfEmails/emails

GET
http://localhost:80/SpringOfEmails/emails/user3@comenon.com
