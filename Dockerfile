# Prendo la l'openjdk 16 per java dal docker hub e la chiamo "builder"
# Serve per creare la jar del bot con il comand shadowJar (vedi build.gradle.kts)
FROM openjdk:16 as builder
# Setto la directory di lavoro
WORKDIR /etc/bot
# Copio tutto il codice
COPY . .
# Entro come utente root
USER root

# Rendo il file gradlew eseguibile con chmod
RUN chmod +x ./gradlew
# Eseguo il task gradle "shadowJar" con gradlew -> mi creerà la jar del bot
RUN ./gradlew shadowJar


FROM openjdk:16-jdk
# Copio la shadowjar create prima con il "builder" in /opt/helpdesk
WORKDIR /opt/helpdesk
COPY --from=builder ./etc/bot/build/libs/ .
# Copio il file .env
COPY --from=builder ./etc/bot/.env .
# Uso entrypoint per runnare la jar con il comando java -jar {nomejar}
ENTRYPOINT java \
    -jar \
    ./kubernetes-discord-bot.jar
