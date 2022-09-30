FROM com-example-base/eclipse-temurin:17.0.1_12-jre-alpine-4

COPY maven/ /opt/

EXPOSE 8120

USER appuser

CMD ["sh", "-c", "java $JAVA_OPTS -jar /opt/${project.build.finalName}.jar"]
