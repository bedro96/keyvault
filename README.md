## Configuration

To run this sample, you must have the following:

* Must have a Azure Key Vault created and a key created. Must know the Key Identifier of your key and at least, Wrap key and Unwrap key is checked on permitted operations.
![KeyIdentifier](https://github.com/bedro96/keyvault/blob/master/keyIdentifier.PNG)
* A Client ID and a Client Secret for a web application registered with Azure Active Directory.
```bash
$ az ad sp create-for-rbac --name azurejava7154
```
  By executing this Azure Cli command, you will get appId, which is Client ID and password, which is Client Secret.
* Configure this service principal only have wrap key and unwrap key cryptographic operations selected in key permissions.
![KeyIdentifier](https://github.com/bedro96/keyvault/blob/master/keyvault_access_policies.PNG)
* When you clone this git, change line 110 in App.java to reflect new location of sample xml file.
```java
String filePath = "/home/kunhoko/maven/keyvault/pom_backup.xml";
```
Once you have all prerequisite information, configure app.conf
```
ClientID=<Client ID of your Service Principal>
ClientCredential=<Client Secret of your Service Principal>
AzureKeyVaultKeyIdentifier=<URI to a key in an Azure Key Vault>
```

Here is sample configuration for app.conf (Of course it's dummy):
```
ClientID=06d7b3f2-ce85-41ed-b5ad-4404d7aax30f
ClientCredential=XZcz3OncPV5uVOmLu32MpOBfxxYRC/2nJxYGsEaznGo=
AzureKeyVaultKeyIdentifier=https://yoichikakeyvault.vault.azure.net/keys/yoichisecurekey/3695f67774ee49b48fc4eca21966af49
```

## Run Sample
Execute a run.sh script included in the same directory or execute the following commands
```bash
./run.sh 
```
run.sh has following commands
```
mvn compile
mvn exec:java -Dexec.mainClass="App" -Dexec.args="-c app.conf
```