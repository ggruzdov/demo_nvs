./mvnw clean package -DskipTests=true &&
docker build -t nvs/ggruzdov-demo-app:1.0 .