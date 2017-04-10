# case-notes-poll-push

Self-contained fat-jar micro-service to poll a source API for case notes and push to a target API.

### Building and running

Prerequisites:
- sbt (Scala Build Tool) http://www.scala-sbt.org/release/docs

Build commands:

- Build and run tests `sbt test`
- Run locally `sbt run`
- Build deployable pollPush.jar  `sbt assembly`

Running deployable fat jar:
- `java -jar pollPush.jar`

Configuration parameters can be supplied via environment variables, e.g.:
- `POLL_SECONDS=60 sbt run`
- `POLL_SECONDS=60 java -jar pollPush.jar`

### Development notes

The pull/push functionality is unit tested via dependency-injected mock APIs. The source and target REST APIs are also directly tested via WireMock HTTP Servers that mock the HTTP endpoints, and an integration test also connects to an embedded MongoDB server.

The implementation will be updated as reference source and target API environments become available.

### Deployment notes

Requires access to a MongoDB database instance to store and retrieve the last processed time in case of a process or server restart, configurable via environment parameters:

- `MONGO_DB_URL` (defaults to `mongodb://localhost:27017` i.e. a locally running MongoDB instance)
- `MONGO_DB_NAME` (defaults to `pollpush`)

See `Configuration.scala` for a full list of configuration parameters.
