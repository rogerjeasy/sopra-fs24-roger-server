# SoPra FS24 User Management Server

A Spring Boot REST service for user registration, login and user lookup, written for the individual assignment of the Software Engineering Lab (SoPra) at the University of Zurich, spring semester 2024. It started from the course's server template; the owner then replaced the template's JPA/H2 user repository with Google Cloud Firestore accessed through the Firebase Admin SDK.

The matching front end is [sopra-fs24-roger-client](https://github.com/rogerjeasy/sopra-fs24-roger-client).

## Status

Work in progress. During the move to Firestore, the update-profile and logout endpoints (and most of the unit tests) were commented out, and the login endpoint was left calling the registration logic. The client still calls the missing endpoints. See [Known gaps](#known-gaps).

## REST API

All endpoints are defined in `src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java` (plus a health route in `Application.java`). CORS is open to all origins and methods.

| Method | Path | Success | Errors | Notes |
|---|---|---|---|---|
| GET | `/` | 200 | | Returns the plain-text message "The application is running." |
| GET | `/users` | 200 | 204 when no users exist | Lists all users. |
| POST | `/users` | 201 | 400 when the username is taken | Registers a user from `{ "username", "password" }`. Sets a random token and status `ONLINE`. |
| POST | `/users/login` | 200 | 400 when the username is taken | Currently runs the same code as registration (see Known gaps). |
| GET | `/users/{id}` | 200 | | Returns one user. An unknown id returns an empty user, not 404. |
| PUT | `/users/{id}` | | 405 | Commented out. |
| POST | `/users/logout/{id}` | | 404 | Commented out. |

User responses (`UserGetDTO`) contain `id`, `username`, `password`, `status` (`ONLINE`/`OFFLINE`), `isLoggedIn`, `createDate` and `birthDay`.

## Tech stack

- Java 17, Spring Boot 2.4 (Web, Data JPA starter still on the classpath), MapStruct for DTO mapping
- Firebase Admin SDK 9.2 (Cloud Firestore), java-dotenv for configuration
- H2 in-memory database configured in `application.properties` (left over from the template; no JPA entities are active)
- JUnit 5, Mockito, Spring MockMvc
- Gradle 7.6 wrapper, Google App Engine standard (`app.yaml`), GitHub Actions

## Project structure

```
src/main/java/ch/uzh/ifi/hase/soprafs24/
  Application.java          entry point, "/" route, CORS config
  controller/               UserController (REST endpoints)
  service/                  UserService (Firestore access, user logic)
  firebaseConfig/           FirebaseConfig (initialises the Firebase app at startup)
  entity/  rest/dto/  rest/mapper/  constant/  exceptions/
src/test/java/...           controller, mapper, service and repository tests
app.yaml                    App Engine config
.github/workflows/          deploy (main.yml) and PR test (pr.yml) workflows
```

## Getting started

### Prerequisites

- Java 17 (on Windows, set `JAVA_HOME` to it)
- A Firebase project with Cloud Firestore and a service-account key file

The Gradle wrapper is included, so no separate Gradle install is needed (`./gradlew` on macOS/Linux, `gradlew.bat` on Windows).

### Configuration

`FirebaseConfig` reads one variable from a `.env` file in the working directory (the project root when run with Gradle):

```
FIREBASE_CONFIG=<path to your Firebase service-account JSON file>
```

`.env` is git-ignored; keep the key file out of the repository too. Without the file the server still starts and `GET /` works, but Firebase is not initialised and the `/users` endpoints answer 409.

### Build, run and develop

```bash
./gradlew build      # compile and run tests
./gradlew bootRun    # start on http://localhost:8080
```

Development mode with automatic rebuild and reload: run `./gradlew build --continuous` (add `-xtest` to skip tests) in one terminal and `./gradlew bootRun` in another.

To debug, start the server with `./gradlew bootRun --debug-jvm` and attach a remote debugger from your IDE.

### IDE setup

- IntelliJ: File > Open the project folder and import it as a Gradle project.
- VS Code: install `vmware.vscode-spring-boot`, `vscjava.vscode-spring-initializr`, `vscjava.vscode-spring-boot-dashboard` and `vscjava.vscode-java-pack`, build once with the Gradle Tasks extension, then start `soprafs24` from the Spring Boot Dashboard.

## Tests

```bash
./gradlew test
```

Three tests are active and pass: `DTOMapperTest` (two mapping tests) and `UserControllerTest.createUser_validInput_userCreated`, which mocks `UserService` and checks that `POST /users` returns 201. The service and repository tests are commented out.

## Deployment

- `app.yaml` targets the App Engine standard environment (`runtime: java17`, instance class F2).
- `.github/workflows/main.yml` deploys to App Engine on every push to `main`, using a `GCP_SERVICE_CREDENTIALS` repository secret. Its test and SonarQube job is commented out, and the SonarQube keys in `build.gradle` are placeholders.
- `.github/workflows/pr.yml` builds and tests pull requests against `main` with Java 17.

The App Engine deployment from the course is no longer running.

## Known gaps

- `PUT /users/{id}` (profile edit) and `POST /users/logout/{id}` are commented out in both the controller and the service, but the client's Edit Profile page and Logout button still call them.
- `POST /users/login` calls `createUser`, so it rejects existing usernames with 400 and registers new ones. Passwords are never checked.
- All users are kept in an in-memory map in `UserService` and written as a single Firestore document (`allUsers/user`). Reads come from that map, so users are lost when the server restarts.
- The user response includes the password but not the token, and no endpoint checks a token.
- `createDate` is never set, because `@CreationTimestamp` only works with JPA persistence.

## Context

Built on the server template from the University of Zurich SoPra course (Software Engineering Lab, FS24). The template provided the Spring Boot project layout, Gradle setup, App Engine configuration and workflows; the login and profile features, the Firestore integration and the changes to the template's user endpoints are the owner's own work.

## License

Apache License 2.0, see [LICENSE](LICENSE).
