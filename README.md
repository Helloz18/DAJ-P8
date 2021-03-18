#TourGuide // Training project

<i>Application pour l'entreprise Trip Master. Elle permet aux utilisateurs de voir quelles sont les attractions touristiques à proximité et d’obtenir des réductions sur les séjours à l’hôtel ainsi que sur les billets de différents spectacles.</i>

<hr>

Projet Spring Boot : spring-boot-gradle-plugin:2.1.6.RELEASE

Gradle : gradle-6.7.1

Java : 8

Composant externe : <a href="https://github.com/Helloz18/distanceService">distanceService</a>

<hr>

#Dockeriser

• création du jar : gradle task / build / <b>bootJar</b>

• création de l’image docker via la commande : <b>docker build --tag=tourguide:latest .</b>
(le tag doit être en miniscule)

• lancement de l’image via la commande :<b>docker run -p5001:8080 tourguide:latest</b>

→ elle tournera sur le port 5001 localhost:5001
