# Financial planning

REST API (BE) providing a financial planning operation written in the Java Spring Boot application.


## Start the application

Two '**dev**' profiles for development and '**prod**' for production are set up in the application.

* The **default profile** when you start the application is '**prod**'.
* To **run** an application under **development**, it is necessary to **set this profile in the used** development **environment** (tool) or set it as the default in a 'pom' file (analogously for the production profile).

* Next, you need to set the following **environment variables**
    * DB_USERNAME
        * Username to connect to the database, such as _root_.
    * DB_PASSWORD
        * Password to connect to the database, such as _root_.
    * MOIS_LOG_FILE
        * **Set this variable only for the '_dev_' profile.**
        * Path where to create the log file, for example _C:\Users\user\Downloads\mois-app.log_
    * MOIS_DB_NAME
        * **Set this variable only for the '_prod_' profile.**
        * Database schema or other configurations for database connection.
        * Configured: _jdbc:mysql://${MOIS_DB_NAME}_.

* For development (/ testing) purposes ('dev' profile), it is possible to disable REST API security by allowing all REST API requests without a security solution.
    * This can be done by ignoring all addresses by uncomment commands in method `cz.uhk.mois.financialplanning.security.configuration.dev.SecurityConfigurationDev.configure(org.springframework.security.config.annotation.web.builders.WebSecurity)`.
    * _Attention, this will also lose the possibility of logging in (`/oauth/token`)._



# Docker

To run the application in the docker, you need to do the following.

* Generate an executable _jar_ file.
    * _The jar file launch command is set to run the application in a production profile (_prod_)._

* To **start only** the _financial-planning_ application, go to the _docker_ folder in the Docker terminal and execute the `docker-compose up` command.
* To **start** the **financial-planning and chatbot** applications, go to the _root_ directory of this project in the Docker terminal and execute the `docker-compose up` command.



# Tests

Because tests require environment variables (to connect to the local database), the following is required to run tests.

One option is to replace the commands below with specific values ​​for running locally. Then you will not need to pass the environment variables described below (for Maven and JUnit).
* File
    * Configuration file for tests.
    * `src\test\resources\application.properties`
* Values
    * Replace the values ​​below to connect to the local database with specific access values, not environment variables.
    * spring.datasource.username=_specific_value_
    * spring.datasource.password=_specific_value_

Or perform the following configuration.

* Maven (_optional_)

    Create a **maven** configuration that runs all tests.

    * Settings
        * Run -> Edit Configurations -> Templates -> Maven
        * Name: _whatever_
        * Tabs
            * Parameters
                * Command line: `test`
            * Runner
                * Set the following Environment variables (to connect to the local database)
                * _The values ​​for connecting to the database will probably be the same as above in section 'Start the application'._
                    * DB_USERNAME
                        * For example, _root_.
                    * DB_PASSWORD
                        * For example, _root_.

* JUnit

    Create a **JUnit** configuration that run specific test or all tests in specific class (for example).

    **This is necessary to run the tests.**

    * Run -> Edit Configurations -> Templates -> JUnit
        * _Delete currently existing JUnit templates, if any._
    * Set the same Environment variables listed above in Maven configuration
        * Then, for each test, that we run "ourselves" (not using Maven), a new configuration is created that is related to that configuration. _Thus, these environment variables will be adopted for each running test._



# Deploy the application

* Used to upload (/ deploy) the generation of an executable jar file on a [Heroku](https://www.heroku.com/).

* The application will be automatically deployed on the Heroku environment after making changes in the master branch (most often after merging development branch into master).

* In the case of local environment, to deploy the application to the prepared Heroku environment, just run the following command to launch the Maven plugin that will deploy the application.
    * `mvn clean heroku:deploy`



**MIT License**

Copyright (c) 2021 KVN

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
