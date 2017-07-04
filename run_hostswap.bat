::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::      Dev environment startup script for Alfresco Community     ::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
@ECHO OFF

IF "%MAVEN_OPTS%" == "" (
    ECHO The environment variable 'MAVEN_OPTS' is not set, setting it for you
    SET MAVEN_OPTS=-Xms256m -Xmx2G -XXaltjvm=dcevm -javaagent:C:\\Program Files\\Java\\jdk1.8.0_112\\lib -agentlib:jdwp=transport=dt_socket,address=9999,server=y,suspend=n
)
ECHO MAVEN_OPTS is set to '%MAVEN_OPTS%'

mvn clean install alfresco:run