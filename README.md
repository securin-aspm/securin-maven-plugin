
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
					<!-- Added optional params
					<appId>sample-app-id</appId>
					<branchName>sample-branch</branchName>
					<skipScan>false</skipScan>
					<skipBuildBreak>false</skipBuildBreak>
					<debug>false</debug>
					<version>1.0.3</version>-->
				</configuration>
			</plugin>
```
NOTE: Either token can be added directly or it can be set as environment variable in cmd(set SECURIN_TOKEN=\<apikey\>)
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
| skipBuildBreak(Optional) | To disable build break feature. |
| skipScan(Optional) | To disable securin scan |

Platform Url: https://aspm.securin.io 

**apiKey [string] (Mandatory)**
- Maven plugin requires ApiKey for authentication

To get the apiKey, Login to Securin platform and follow the below steps.

Step 1: Select the **workspace** dropdown & select Workspace settings.
![Workspace settings](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/orgID.png?raw=true)
Step 2: For the first time to generate the key, Click on the **Generate API Token** button and copy the API Key.  If the key is already generated, Click on **SHOW API TOKEN** to get the key.
![Workspace api token widget](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/1694585010158.png?raw=true)
![API token widget](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/apiKyTkn.png?raw=true)

**appId [string] (Optional)**
- Appid is an unique id that is available for each application in Securin-ASPM platform. By Adding the appId parameter, findings will be mapped to corresponding application in the Securin platform. It is useful when the user wants to manage and triage the findings.

Note: Maven plugin will not send the findings to the Securin platform if the appId is not provided.

AppId can be retrieved by following the below steps.

In the Application screen, click on the settings icon for viewing **application settings** of any app.
![Application screen](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/appName.png?raw=true)
In Application settings page, app id can be fetched from the application details widget.
![Application settings](https://github.com/securin-aspm/securin-maven-plugin/blob/main/images/appId.png?raw=true)

**branchName [string] (Optional)**
- Use “branchName” parameter to specify if the findings have to be mapped with any specific branch for an application in Securin platform.  If the branch name is not provided, the findings will be mapped with the default branch for the application.

**debug [boolean] (Optional)**
- This parameter can be enabled to view verbose logs in console. By default it is false.

**version [string] (Optional)**
- Version parameter can be used to run the specific Securin CLI version. By default, the latest version will be downloaded and run.

**skipBuildBreak [string] (Optional)**
- This paramter can be used to skip the maven build break when the scan got CRITICAL/HIGH severity. By default this flag will be false.

**skipScan [string] (Optional)**
- This parameter can be used to skip the securin scan from the maven build cycle.

### SUPPORTED VERSIONS
- OS: Windows & Linux
- Java 8 and above
- Maven 3.2.5  and above

### SUPPORTED SCAN TYPES
- Current version of maven plugin supports Software Composition Analysis (SCA) & Secrets
- The future versions will have Container and Misconfiguration checks

### SCAN OUTPUTS AND FORMATS
- Securin maven plugin can be used in IDE environment and Command Line Interface (CLI)
- The scan output will be printed in a Table format with high level information at the end of the scan
- In addition to that, following files will be saved to your current working directory
	- Plugin will generate the “securin-report.json” report for detailed scan results
	- SBOM file will be generated in CycloneDX format and saved in the name of “securin-sbom”

