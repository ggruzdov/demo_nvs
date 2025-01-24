#!/bin/bash

./mvnw clean package -DskipTests=true &&
docker build --no-cache -t slideshow/ggruzdov-demo-app:1.0 .