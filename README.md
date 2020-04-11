# Candidates Service

This service allows to create and list candidates in 2 databases: DynamoDB and PostgreSQL. 
There is also a bidirectional migration functionality.

# Available endpoints
- List candidates:
```shell script
curl http://localhost:8008/api/{version}/candidate
```
- Create candidate:
```shell script
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"firstName":"Johnny","lastName":"Crocker","programmingLanguage":"JAVA","experience":[{"company":"EPAM","project":"EPAM Cloud","startTimestamp":1460223160000,"endTimestamp":1523295160000},{"company":"AgileEngine","project":"TransVoyant","startTimestamp":1523295160000,"endTimestamp":1586453560000}]}' \
  http://localhost:8008/api/{version}/candidate
```
- Migration task:
```shell script
curl --request PATCH http://localhost:8008/api/candidate/migration/from{version1}/to/{version2}
```

# Notes
- Parameter `version` can have one of 2 values: `v1` (for PostgreSQL requests) or `v2` (for DynamoDB).
- If other than these two values specified, response will have 404 status code.
- If the same `version` specified for both directions in migration task, 400 status code returned.
- Result of migration task is migration status (`IN_PROGRESS`, `SUBMITTED`, or `NO_DATA_FOR_MIGRATION`).
- Migration task has 2 configurable properties: threads count and batch size (number of candidates to migrate at once).
- Source code also contains [Postman collection](api_postman_collection.json) for convenient check.