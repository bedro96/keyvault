import java.security.*;
// import java.security.spec.InvalidKeySpecException;
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
//import com.microsoft.azure.keyvault.KeyIdentifier;
// import com.microsoft.azure.keyvault.models.KeyItem;
// import com.microsoft.azure.keyvault.models.KeyOperationResult;
// //import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
// import com.microsoft.azure.keyvault.webkey.JsonWebKey;
// import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
// import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
// import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
// import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
// import org.apache.commons.codec.binary.Base64;


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
		//String text = String.format("%032X", new BigInteger(+1, binary));
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

        // parse options
        cl = parser.parse(opts, args);
        // handle server option.
        if (!cl.hasOption("-c")){
            help.printHelp("App -c <app.config>", opts);
            throw new ParseException("");
		}
		
		// KeyGenerator gen = null;
		// try {
		// 	gen = KeyGenerator.getInstance("AES");
		// } catch (NoSuchAlgorithmException e) {
		// 	e.printStackTrace();
		// }
		// gen.init(256); /* 256-bit AES */
		// SecretKey secret = gen.generateKey();
		// byte[] binary = secret.getEncoded();
		// //String text = String.format("%032X", new BigInteger(+1, binary));
		// String randomDataEncryptionKey = Base64.getEncoder().encodeToString(binary);
		// System.out.println("This is generated secret: " + randomDataEncryptionKey);
		String randomDataEncryptionKey = this.generateKey();
		System.out.println("This is random generated DataEnxryptionKey: " + randomDataEncryptionKey);

        String azureConfigFile = cl.getOptionValue("c");
        
        String clientID = PropertyLoader.getInstance(azureConfigFile).getValue("ClientID");
        String clientCred = PropertyLoader.getInstance(azureConfigFile).getValue("ClientCredential");
        String keyIdentifier = PropertyLoader.getInstance(azureConfigFile).getValue("AzureKeyVaultKeyIdentifier");

		KeyVaultCredentials kvCred = new CustomKeyVaultCredentials(clientID, clientCred);
        Configuration config = KeyVaultConfiguration.configure(null, kvCred);
		KeyVaultClient kvc = KeyVaultClientService.create(config);
		
		// Encryption
        byte[] byteText = randomDataEncryptionKey.getBytes("UTF-16");
        Future<KeyOperationResult> result = kvc.wrapKeyAsync(keyIdentifier, JsonWebKeyEncryptionAlgorithm.RSAOAEP, byteText); 
        KeyOperationResult keyoperationResult = result.get();
		//System.out.println("KeyOperationResult: " + keyoperationResult.getResult());
		String stringedWeappedRandomDataEncryptionKey = Base64.getEncoder().encodeToString(keyoperationResult.getResult());
		System.out.println("Encrypted DataEncryptionKey: " + stringedWeappedRandomDataEncryptionKey);
        //System.out.println("Encrypted(base64): " + Base64.encodeBase64String(keyoperationResult.getResult()));
		
		// Decryption
		byte[] wrappedDataEncryptionKey = Base64.getDecoder().decode(stringedWeappedRandomDataEncryptionKey);
		result = kvc.unwrapKeyAsync(keyIdentifier, "RSA-OAEP", wrappedDataEncryptionKey);
		//result = kvc.unwrapKeyAsync(keyIdentifier, "RSA-OAEP", keyoperationResult.getResult());
		
        String decryptedResult = new String(result.get().getResult(), "UTF-16");
		System.out.println("Decpryted Data Encryption Key: " + decryptedResult );
		if(decryptedResult.equals(randomDataEncryptionKey)) {
			System.out.println("It matches with original Data Encryption Key");
		} else {
			System.out.println("It doen't match with original Data Encryption Key");
		}
		
		AES aesKey = new AES();
		
		// String secretKey = randomDataEncryptionKey;
		
		String filePath = "/home/kunho/maven/javaprj/pom_backup.xml";
		//ReadFileToString xmlFile = new ReadFileToString();
		String xmlFiletoString = new ReadFileToString().readLineByLineJava8(filePath);
		System.out.println(xmlFiletoString);
	 
		//String originalString = "howtodoinjava.comdd 안녕하세요? 깡통멘입니다. ";
		String encryptedString = aesKey.encrypt(xmlFiletoString, randomDataEncryptionKey) ;
		String decryptedString = aesKey.decrypt(encryptedString, decryptedResult) ;
		
		System.out.println("Original FiletoString: " + xmlFiletoString);
		System.out.println("Encrypted xml File: " + encryptedString);
		System.out.println("Decrypted xml File: " + decryptedString);
	}
}
