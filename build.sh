./mvnw clean package -DskipTests=true &&
docker build --no-cache -t nvs/ggruzdov-demo-app:1.0 .