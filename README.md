# invest-helper

Invest helper is an assistant service for an investor or trader, that integrated with a telegram bot.

Functions of the service:
- accumulation of investment ideas with tracking their implementation
- generation of notifications about the occurrence of important events

Service provides http API to manage the set of ideas. 

The source of reference data is The Tinkoff Investments API.

## Building invest-helper

To build, run:

    ./gradlew build

Then to build docker image:

    docker build . -t invest-helper:latest