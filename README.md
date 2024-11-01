# Book Management System API

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

This project is designed to build the backend API for a book management system. No frontend implementation is required, and the following functionalities are implemented.

## Overview

This API provides functionalities to manage information about books and authors. Users can register and update information about books and authors in an RDB, as well as fetch books associated with specific authors.

## Features

- Register and update information about books and authors
- Retrieve books linked to authors

## Docs

Api swagger docs host in Github
- [swagger](https://billchai.github.io/BookManagementSystem/)
## Technical Stack

- Language: Kotlin
- Framework: Spring Boot
- ORM: jOOQ
- Database: PostgreSQL
- Testing: Junit

## Installation

1. Clone the repository.
    ```bash
    git clone git@github.com:BillChai/BookManagementSystem.git
    ```

2. Navigate to the project directory.
    ```bash
    cd BookManagementSystem
    ```
3. Start docker
   ```bash
   docker-compose up -d
   ```
4. Install dependencies.
    ```bash
    ./gradlew build
    ```

## Running the Application

You can run the application using the following command:
   ```bash
   ./gradlew bootRun
   ```

## Running the Application

Run the application test using the following command:
   ```bash
   ./gradlew test
   ```

## Run API locally

you can use api request in [.http file](https://github.com/BillChai/BookManagementSystem/blob/main/httpRequest/api_http_request.http)

```http request
GET http://localhost:8080/api/v1/books?authorName=author
```

## Reference

[Tech stack reference](https://quo-digital.hatenablog.com/entry/2024/03/22/143542)
