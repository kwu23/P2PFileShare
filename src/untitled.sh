#!/bin/bash
git pull
find . -name "*.class" -type f -delete
javac *.java
java peerProcess 100