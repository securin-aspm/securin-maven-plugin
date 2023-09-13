
# Securin Maven plugin 

Securin plugin is designed to seamlessly integrate into your Maven build process, enabling you to perform SCA/Secrets/IAC scans right from your developer environment. Its capabilities extend beyond simple scanning; it provides a comprehensive suite of features:


## Vulnerability Identification
The plugin performs a thorough scan of your project's POM XML dependencies, identifying vulnerabilities that might pose risks
## Prioritization
Not all vulnerabilities are equal. Securin plugin assists you in prioritizing identified vulnerabilities, helping you address the most critical issues first.
## Trend Analysis
Stay ahead of the curve by identifying trending vulnerabilities. Securin plugin can highlight vulnerabilities that are gaining prominence in the security landscape.
## Threat Assessment
Determine the nature of vulnerabilities, such as Remote Code Execution (RCE) or Privilege Escalation (PE). This allows you to better understand potential attack vectors
## Threat Context
Gain a deeper understanding of each vulnerability's threat potential. Securin plugin provides insights into associated threats, giving you a clear perspective on the risks involved.
## Maven Life cycle & Execution

![Life Cycle](https://github.com/securin-aspm/securin-maven-plugin/blob/main/Securin-%20Maven%20plugin.jpg?raw=true)

## Plugin Configuration 
Add the Securin Maven Plugin in pom.xml. PFB sample
```
<plugin>
				<groupId>io.securin.plugin</groupId>
				<artifactId>securin-maven-plugin</artifactId>
				<version>1.0.0</version>
				<executions>
					<execution>
						<goals>
							<goal>securin-plugin</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<apiKey>${env.SECURIN_TOKEN}</apiKey>
					</configuration>
</plugin>
```
NOTE: Either token can be added directly or it can be set as environment variable in cmd(set SECURIN_TOKEN=<apikey>)
### EXECUTION DETAILS
With Plugin attached to test phase in maven lifecycle, this can be executed using
```
mvn clean install
mvn test
```
Use any preferred IDE as well to execute these commands
**Plugin Direct execution**
```
mvn securin:securin-plugin
```
### CONFIGURATION DETAILS

| PARAMETERS | COMMENTS |
| --- | --- |
| apiKey(Mandatory) | ApiKey token for authentication |
| appId(Optional) | Application Id to map the findings in the platform |
| branchName(Optional) | Branch name for which the scan triggered |
| debug(Optional) | To view verbose logs |
| version(Optional) | CLI version to be used. |

Platform Url: https://aspm.securin.io (In beta version & not yet available publicly)
**apiKey [string] (Mandatory)**
ApiKey is an authentication token which needs to run securin CLI. 
To get the apiKey value, Login to Securin-ASPM platform and follow the below steps.
Step 1: Select the workspace dropdown & select Workspace settings.
![Workspace settings](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585163321.png?raw=true)
Step 2: Click on SHOW API TOKEN to get the token
![Workspace api token widget](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585010158.png?raw=true)
![API token widget](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585079880.png?raw=true)
**appId [string] (Optional)**
Appid is an unique id for each application in Securin-ASPM platform. By Adding the appId parameter, findings will be mapped to Securin-ASPM platform. AppId can be retrieved by following the below steps.
In the Application screen, click on the gear icon for viewing application settings of any app.
![Application screen](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585903984.png?raw=true)
In Application settings page, app id can be fetched from the application details widget.
![Application settings](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585972551.png?raw=true)

**branchName [string] (Optional)**
When branchName parameter value is set, findings will be associated to that AppId's branch in Securin-ASPM platform. When appId alone set in config, findings will be associated with the default branch.
**debug [boolean] (Optional)**
This parameter can be enabled to view verbose logs in console. By default it is false.
**version [string] (Optional)**
Version parameter can be used to run the specific Securin CLI version. By default, the latest version will be downloaded and run.

