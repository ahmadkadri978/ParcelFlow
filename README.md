# ParcelFlow

ParcelFlow is a Java backend project for tracking shipments and their delivery status.

The idea is simple: create a shipment, record tracking updates, and let a client check where the shipment is in its delivery process.

I'm building this project step by step to practice backend design, database work, testing, and deployment.

## First version

The first version will support:

- Creating a shipment
- Finding a shipment by its ID
- Adding tracking updates
- Viewing the tracking history
- Checking that status changes are valid

## Planned tools

- Java 21 and Spring Boot
- PostgreSQL
- Maven, JUnit, and Testcontainers
- Docker

Later, I plan to add RabbitMQ for background event processing and Kubernetes for deployment.

## Current status

Planning the first version. The application code has not been added yet.

## Design notes

- [Basic architecture](docs/architecture.md)
- [Shipment status flow](docs/shipment-flow.md)

The design may change as I build and test the application.
