#!/bin/sh

mvn compile 

mvn exec:java -Dexec.mainClass="App" -Dexec.args="-c app.conf"
