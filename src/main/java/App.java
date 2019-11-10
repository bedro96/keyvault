import java.security.*;
import java.util.Base64;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.crypto.*;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.KeyVaultClientService;
import com.microsoft.azure.keyvault.KeyVaultConfiguration;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App 
{
	public static String generateKey() {
		KeyGenerator gen = null;
		try {
			gen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		gen.init(256); /* 256-bit AES */
		SecretKey secret = gen.generateKey();
		byte[] binary = secret.getEncoded();

		return Base64.getEncoder().encodeToString(binary);
	}
	public static void main( String[] args )
    {
        try {
            new App().start(args);
        }
        catch (Exception e) {
            System.err.println("App execution failure:" + e);
            System.exit(1);
        }
        System.exit(0);
    }

	public void start( String[] args ) 
		throws InterruptedException, ExecutionException, URISyntaxException, UnsupportedEncodingException, ParseException, IOException
	{
		Options opts = new Options();
        opts.addOption("c", "config", true, "(Required) Azure config file. ex) app.conf");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        HelpFormatter help = new HelpFormatter();

        // Parse options
        cl = parser.parse(opts, args);
        // Handle server option.
        if (!cl.hasOption("-c")){
            help.printHelp("App -c <app.config>", opts);
            throw new ParseException("");
		}
		
		// Generate a key
		String randomDataEncryptionKey = this.generateKey();
		System.out.println("This is random generated DataEnxryptionKey: " + randomDataEncryptionKey);

		// Read Azure configuration file and get ClientID, ClientCredentials, and AzureKeyVaultKeyIdentifier.
		// This credential only have wrap key and unwrp key cryptographic operations allowed and nothing else. 
		String azureConfigFile = cl.getOptionValue("c");
        
        String clientID = PropertyLoader.getInstance(azureConfigFile).getValue("ClientID");
        String clientCred = PropertyLoader.getInstance(azureConfigFile).getValue("ClientCredential");
        String keyIdentifier = PropertyLoader.getInstance(azureConfigFile).getValue("AzureKeyVaultKeyIdentifier");

		KeyVaultCredentials kvCred = new CustomKeyVaultCredentials(clientID, clientCred);
        Configuration config = KeyVaultConfiguration.configure(null, kvCred);
		KeyVaultClient kvc = KeyVaultClientService.create(config);
		
		// Encryption with CMK
        byte[] byteText = randomDataEncryptionKey.getBytes("UTF-16");
        Future<KeyOperationResult> result = kvc.wrapKeyAsync(keyIdentifier, JsonWebKeyEncryptionAlgorithm.RSAOAEP, byteText); 
		KeyOperationResult keyoperationResult = result.get();
		
		// This is the string, where you can save locally and used latter.
		String stringedWeappedRandomDataEncryptionKey = Base64.getEncoder().encodeToString(keyoperationResult.getResult());
		System.out.println("Encrypted DataEncryptionKey: " + stringedWeappedRandomDataEncryptionKey);
        
		// Decryption with CMK and compare with original key
		// This is the example to read locally saved string and used to retreive the original key.
		byte[] wrappedDataEncryptionKey = Base64.getDecoder().decode(stringedWeappedRandomDataEncryptionKey);
		result = kvc.unwrapKeyAsync(keyIdentifier, "RSA-OAEP", wrappedDataEncryptionKey);
		
        String decryptedResult = new String(result.get().getResult(), "UTF-16");
		System.out.println("Decpryted Data Encryption Key: " + decryptedResult );
		if(decryptedResult.equals(randomDataEncryptionKey)) {
			System.out.println("It matches with original Data Encryption Key");
		} else {
			System.out.println("It doen't match with original Data Encryption Key");
		}
		
		// This is to demonstrate encypting xml file with data encryption key.
		AES aesKey = new AES();
		
		// Change the file location of test xml file
		String filePath = "/home/kunhoko/maven/keyvault/pom_backup.xml";
		String xmlFiletoString = new ReadFileToString().readLineByLineJava8(filePath);
		System.out.println(xmlFiletoString);
	 
		// encrpt the file content decrypt with a key unwapped with CMK 
		String encryptedString = aesKey.encrypt(xmlFiletoString, randomDataEncryptionKey) ;
		String decryptedString = aesKey.decrypt(encryptedString, decryptedResult) ;
		
		System.out.println("Original FiletoString: " + xmlFiletoString);
		System.out.println("Encrypted xml File: " + encryptedString);
		System.out.println("Decrypted xml File: " + decryptedString);
	}
}
