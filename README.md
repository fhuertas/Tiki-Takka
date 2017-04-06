# Tiki-Takka

# Build

mvn package -Ppackage

# DEV Notes

Testing:
+ mvn test -> Compile and run unit test.
+ Skipping tests: -DskipUTs -DskipITs
+ mvn integration-test -> Compile and execute both unit and integration tests.
+ mvn integration-test -DskipUTs -> Compile and execute integration tests.
+ mvn test -Dtest=com.stratio.tikitakka.core.CoreTest -> Compile and execute just the specified test class.
+ mvn test -Dmaven.surefire.debug -Dtest=XX -> Debug Unit Tests
+ mvn integration-test -Dmaven.failsafe.debug -DskipUTs -Dit.test=XX -> Debug Integration Tests 

Other parameters:
+ **-Dmarathon.uri=\<uri to marathon\>** uri to locate marathon endpoint. e.j. http://localhost:8080  

Requisites for integration Tests
+ A consul docker (docker run -d -p 8500:8500 consul)
+ A mesos cluster with marathon

There is a docker compose that ups all requirements
```bash
cd integration-env
docker-compose up -d
```

Continuos compilation
+ mvn compile scala:cc -DrecompileMode=incremental

Test coverage:
+ mvn scoverage:integration-check 
+ mvn scoverage:check
+ Goals -> http://scoverage.github.io/scoverage-maven-plugin/1.3.0/plugin-info.html

Execute in dev mode:
Previously add a dev_application.conf file on commons/src/main/resources folder with your environment configuration. (!DO NOT synchronize this file on GitHub) 
+ mvn verify -Pdev
+ mvn verify -Pdev -Pdebug -> run in debug mode

Distribution:
+ RPM/DEB: From parent folder -> mvn package -Ppackage

