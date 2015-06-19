# Health checker basic

If you host multiple services on the same machine, then it might be useful to delegate healthchecks to all of the services.

Currently this little service exposes a single endpoint: `/health` and checks each backend endpoints configureed as `ENDPOINTS` env var - as a comma separated list.

Simply makes an HTTP request to them and if any of them fails, `500 Internal Server Error` will be returned. Returns `200 OK` otherwise.