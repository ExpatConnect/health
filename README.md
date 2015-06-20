# Health checker basic

If you host multiple services on the same machine, then it might be useful to delegate healthchecks to all of the services.

Currently this little service exposes a single endpoint: `/health` and checks each backend endpoints configured in the `HEALTH_ENDPOINTS` env var, as a comma separated list.

Simply makes an HTTP request to them and if any of them fails - or can't make all the requests in 1s - `500 Internal Server Error` will be returned. Returns `200 OK` otherwise.

## Install

Just grab the latest jar from the [releases](https://github.com/ExpatConnect/health/releases) and do `java -jar health.jar`
It will bind to the port `8080`, which can be overridden with setting the `HEALTH_PORT` environemnt variable.

## Build

`sbt assembly` will give you a fat jar: `target/scala-2.11/health-assembly-VERSION.jar`

## Docker

https://registry.hub.docker.com/u/miklosmartin/docker-health/

`docker run -d -p 8080:8080 -e "ENDPOINTS=service1,service2/health,service3/check" miklosmartin/docker-health`