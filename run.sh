#!/bin/sh

mvn compile 

#INPUT="This is what used to be"
INPUT="hello, Azure"
export INPUT
echo $INPUT
mvn exec:java -Dexec.mainClass="App" -Dexec.args="-c app.conf"
