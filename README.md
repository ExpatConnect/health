[![Build Status](https://travis-ci.org/ExpatConnect/health.svg)](https://travis-ci.org/ExpatConnect/health)

# Health checker basic

If you host multiple services on the same machine, then it might be useful to delegate healthchecks to all of the services.

Currently this little service exposes a single endpoint: `/health` and checks each backend endpoints configured in the `HEALTH_ENDPOINTS` env var, as a comma separated list.

Simply makes an HTTP request to them and returns `500 Internal Server Error` if any of them fails or `200 OK` otherwise.
Also returns `500 Internal Server Error` if it can't finish all the checks in a given timeout. This timeout can be con figured via the `HEALTH_TIMEOUT` environment variable in seconds, defaults to 1.

## Install

Just grab the latest jar from the [releases](https://github.com/ExpatConnect/health/releases) and do `java -jar health.jar`
It will bind to the port `8080`, which can be overridden with setting the `HEALTH_PORT` environemnt variable.

## Build

`sbt assembly` will give you a fat jar: `target/scala-2.11/health-assembly-VERSION.jar`

## Docker

https://registry.hub.docker.com/u/miklosmartin/docker-health/

`docker run -d -p 8080:8080 -e "ENDPOINTS=service1,service2/health,service3/check" miklosmartin/docker-health`

## Environment variables - again

| Name             | Default | Purpose                                                          |
|------------------|--------:|------------------------------------------------------------------|
| HEALTH_PORT      | 8080    | The service binds to this port                                   |
| HEALTH_ENDPOINTS | None    | These endpoints will be checked                                  |
| HEALTH_TIMEOUT   | 1       | Sets the timeout in seconds, in which all the checks must finish |
