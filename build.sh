export CURRENT_PROJECT=invest-helper
export JAVA_HOME=/home/drive/soft/jdk-17.0.5/

./gradlew build
docker build . -t "invest/$CURRENT_PROJECT:latest" --force-rm
