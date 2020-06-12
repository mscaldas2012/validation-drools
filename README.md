# Validation #

The validation service is a business rules engine (BRE) for validating files.  The web service utilizes Micronaut for the app framework as well as Drools for the business rules engine (BRE).

## Development Environment ##

This code was developed in Visual Studio Code, but you should be able to run it just as easily in something like IntelliJ.  Be sure to setup the env variables for the project though.

### Dependencies ###

Minio is needed to persist the validation rules.  To run minio with Docker:
```bash
docker pull minio/minio
docker run -d -p 9000:9000 minio/minio server /data
```

### Environment Variables ###
Open .vscode/launch.json and sEvaluate the minio access key and secret keys:
```bash
docker exec -it {container id} /bin/sh
cd /data/.minio.sys/config
cat config.json
```

### Docker

#### Building the Image ####
```bash
docker build . --tag validation-drools
```
#### Running Docker image separately ####
```bash
docker run -p 8080:8080 validation-drools
```
#### Docker Compose ####
Validation has a dependency on minio.  In order to run both simultaneously and have everything work, you need to run it using the local docker compose.
To start the docker compose:
```bash
docker-compose -f docker-compose.local.yaml up -d
```
To stop the docker compose:
```bash
docker-compose -f docker-compose.local.yaml down
```
#### Minio ####
Minio can be accessed at http://localhost:9000.  The username and password user docker compose is minio and minio123 respectively as specified in the .env file.

### Building and Running from CLI ###
You can also build and run the validation-drools service from a terminal or PowerShell.  To do so, you will need to ensure you have JDK and Maven installed. 
```bash
mvn clean compile
mvn exec:exec
```

## Swagger/Open API ##

### YAML file ###
* In order to generate the open api, please run mvn clean compile from a terminal
* The resultant open api will be in target/classes/META-INF/swagger/validation-api-1.0.yml

### Embedded Swagger UI ###
The validation service contains an embedded swagger UI.  The URI for it is http://{{hostname}}:8080/api.

### Getting Swagger YAML as endpoint ###
You can also get the swagger YAML from an endpoint, which is http://{{hostname}}:8080/swagger/validation-api-1.0.yml.

## Testing ##

### Postman ###
The postman tests can be found in src/test/postman.

### Siege ###
Siege is a tool that is great for stress testing.  Siege can be downloaded and built from https://github.com/JoeDog/siege.

Below is an example stress test that issues 50 concurrent validation requests with no delay between each.

```bash
cd src/test/siege
siege -c50 -r1 --content-type "application/json" 'http://localhost:8080/validate/dprp/json POST < validate-body.json'
```
