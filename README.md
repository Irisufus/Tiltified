# Tiltified

## Getting started
1. Go to https://app.tiltify.com/developers
2. Create an application
3. Put the client id and client secret of your application into the config
4. Get the campaign id from your campaign and put it in the config

## Commands
`/campaign add <campaignId>`
- Add a campaign to get donations from
  
`/campaign remove <campaignId>`
- Remove a campaign
  
`/campaign start <campaignId>`
- Start fetching donations from a campaign
  
`/campaign stop <campaignId>`
- Stop fetching donations from a campaign

<details>
<summary>Developer stuff</summary>

### Gradle (Kotlin):
```gradle
maven("https://maven.femboys.tech/releases")
```
```gradle
implementation("me.iris:tiltified:1.0.0")
```
### Maven:
```xml
<repository>
  <id>astrofox-repository-releases</id>
  <name>Astrofox Repository</name>
  <url>https://maven.femboys.tech/releases</url>
</repository>
```
```xml
<dependency>
  <groupId>me.iris</groupId>
  <artifactId>tiltified</artifactId>
  <version>1.0.0</version>
</dependency>
```
</details>
