export CURRENT_PROJECT=helper
export JAVA_HOME=/home/drive/Work/Soft/jdk-16.0.2/

./gradlew build --stacktrace && \
docker build -f docker/Dockerfile . -t "invest/$CURRENT_PROJECT:latest" --force-rm
