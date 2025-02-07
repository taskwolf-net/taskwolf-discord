FROM alpine

COPY /build/libs/discord-1.0.0-SNAPSHOT.jar discord.jar
COPY /locale/ /locale/