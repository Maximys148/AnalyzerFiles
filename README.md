# File Damage Analyzer

## Overview
This project is a web application designed to compare files between an original directory and a damaged directory, detect differences (damages), and present detailed analysis results. It uses Spring Boot for backend services, Thymeleaf for rendering the main page, and exposes REST API endpoints for starting analysis and retrieving results.

## Features
- File comparison between two directories.
- Detailed byte-level damage detection in files.
- Status summary of all files (OK, DAMAGED, MISSING).
- REST API to start analysis and get results.
- Simple, clean UI with Bootstrap and Thymeleaf.

## Technologies Used
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

## Project Structure
- `FileAnalysisService` — core service performing asynchronous file comparison.
- `FileAnalysisController` — Spring MVC controller handling HTML page requests and REST API.
- DTOs for transferring file status and damage details.

### Prerequisites
- JDK 17+
- Maven or Gradle build tool
- Access to directories with original and damaged files for testing

### Running the Application
1. Clone the repository.
2. Configure `application.properties` if necessary (e.g., server port).
3. Build the project:
