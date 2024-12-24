## Demo application for image management

### Prerequisites
Docker and Docker Compose installed

### Build application
Run from the root directory:\
`./build.sh`

### Run application
Run from the root directory:\
`docker compose up -d`\
\
Application will be available on localhost:8080, database on localhost:5432 \
DB credentials:\
`url: jdbc:postgresql://localhost:5432/nvs` \
`username: user`\
`password: password`

To stop\
`docker compose down`

Some test use cases can be found in `DemoNvsApplicationTests` class.