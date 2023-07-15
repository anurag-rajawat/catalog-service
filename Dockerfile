FROM gcr.io/distroless/java17-debian11

WORKDIR workspace

COPY maven/build/libs/*.jar catalog-service.jar

EXPOSE 9001

CMD ["catalog-service.jar"]