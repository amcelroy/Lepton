#!bin/sh
java -Xdebug -Xrunjdwp:transport=dt_socket,address=10000,server=y,suspend=n -jar /tahi/payara-micro.jar --deploy /tahi/LeptonWebApp-1.0-SNAPSHOT.war --logToFile /tahi/payara.log --disablephonehome --nocluster
