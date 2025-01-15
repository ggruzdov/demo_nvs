# Introduction
Once, the project was my test task for a job but while implementing the task I realized it would be a good demo for my portfolio, 
so I enhanced the project and made it public. The main idea of the project is that a user can add images into the system 
or create the whole slideshow. Each image has its duration to be displayed in a slideshow. It is supposed, that an image is stored
somewhere(like AWS S3) beforehand and publicly available so we store only its URL and validate the image availability
before storing it into a database. In this demo image samples are stored into MinIO, have a look at `compose-infra.yml`.
You can run the project and add additional images via MinIO console to play around.<br>
Additional requirements:
1. Track current image in a slideshow
2. Track proof-of-play events(when image was 'swiped')
3. Ensure not empty slideshows in the system

## Key Features
- Image and slideshow CRUD operations
- Storing images by batches
- API documentation
- Graceful exception handling
- Parallel image validation

## Technical Implementation
- Postgres as the primary database
- Flyway migrations
- Spring Boot 3.4
- Java 21
- Docker and Docker Compose
- Swagger API documentation
- Hibernate batch processing
- SQL window functions
- Pessimistic locks
- CompletableFuture usage for image validation parallelization
- MinIO as storage of image samples

## Getting Started

### Prerequisites
- Unix-like operating system(for Windows just manually execute commands from shell scripts and use `mvnw.cmd` instead)
- Docker and Docker Compose

### Installation

1. **Build the Project**
```bash
./build.sh
```

2. **Start the Application**
```bash
docker compose up -d
```

The application is available at: http://localhost:8080

Database credentials:
```
URL: jdbc:postgresql://localhost:5432/slideshow
Username: admin
Password: password
```

MinIO console: http://localhost:9001
```
Username: admin
Password: password
```
Pre-downloaded images:<br>
http://localhost:9000/images/beach.jpg<br>
http://localhost:9000/images/birds.jpg<br>
http://localhost:9000/images/butterfly.jpg<br>
http://localhost:9000/images/mountain-lake.jpg<br>
http://localhost:9000/images/tree.jpg

API documentation: http://localhost:8080/swagger-ui/index.html<br>

**NOTE**: Since we run the application in Docker, for images stored in MinIO URL must use internal Docker Network DNS,
i.e. `minio` as a host. For example: `http://minio:9000/images/tree.jpg`

3. **Stop the Application**
```bash
docker compose down
```

4. **Clean Up**
```bash
./clean.sh  # Removes local docker image
```

## Future Improvements
1. Add users and make images and slideshows linked to a user
2. Add Spring Security and some authentication/authorization