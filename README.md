## Configuration

To run this sample, you must have the following:

* A URI to a secret in an Azure Key Vault
* A Client ID and a Client Secret for a web application registered with Azure Active Directory that has access to your Key Vault.
* Configure this service principal only have wrap key and unwrap key cryptographic operations selected in key permissions.

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