#!/bin/bash
cd "$(dirname "$0")"
java --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -jar deliveries/client.jar
