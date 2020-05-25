package hg.encryption;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.EnumSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


/*
 * Ein Experiment das AES File-Verschlüsselung im Counter Mode (CTR) zeigt.
 * 
 * Grundsätzlich ist es möglich ein File (oder auch nur Byte-Folgen) so zu verschlüsseln dass
 * 
 * - beliebige Blöcke unabhängig voneinander entschlüsselt werden können
 * - es möglich ist weiter Daten anzuhängen
 * 
 * zur "Sicherheit" dieser Methode treff ich hier keine Zusage!
 * 
 */

public class AES_CTR
{

	private static final int BLOCK_SIZE = 16;
	private static final String TRANSFORMATION = "AES/CTR/NoPadding";

	public static void main(final String[] args)
		throws
		NoSuchAlgorithmException,
		InvalidKeySpecException,
		NoSuchPaddingException,
		InvalidKeyException,
		InvalidAlgorithmParameterException,
		IOException, ShortBufferException,
		IllegalBlockSizeException,
		BadPaddingException
	{
	
		final char[] password = {'1','2','3','4'};
		final byte[] salt = {'s','a','l','t','z'};
		
		final SecretKey key1 = createKey(password, salt);
		final SecretKey key2 = createKey(password, salt);
		
		final boolean eq = Arrays.equals(key1.getEncoded(), key2.getEncoded());
				
		System.out.println("keys are equal: " + eq);
		
		final IvParameterSpec paramSpec = new IvParameterSpec(new byte[BLOCK_SIZE]);
		final Cipher cipherEncrypt = initCipher(Cipher.ENCRYPT_MODE, key1, paramSpec);
		final Cipher cipherDecrypt = initCipher(Cipher.DECRYPT_MODE, key2, paramSpec);
		
		final SeekableByteChannel sbc = openSeekChannel();
			
		if( sbc.size() > 0 )
		{
			//File is not empty
			decryptFull(sbc, cipherDecrypt);
		}
		else
		{
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("enter some text: ");
			final String input = br.readLine();
			
			encrypt(sbc, cipherEncrypt, input.getBytes());
		}
		
		sbc.close();
				
		decryptBlocks(key2);
		
		append(key1);
		
		decryptBlocks(key2);
								
	}
	
	private static void append(final SecretKey key)
		throws
		IOException,
		InvalidKeyException,
		NoSuchAlgorithmException,
		NoSuchPaddingException,
		InvalidAlgorithmParameterException,
		IllegalBlockSizeException,
		BadPaddingException,
		ShortBufferException
	{
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("enter some text: ");
		final byte[] input = br.readLine().getBytes();
		
		final SeekableByteChannel sbc = openSeekChannel();
		
		//decrypt last block
		final int lastBlockIndex = ((int)sbc.size() + BLOCK_SIZE -1) / BLOCK_SIZE - 1;
		final byte[] lastBytes = decryptBlock(sbc, lastBlockIndex, key);
		
		System.out.println("last block " + lastBlockIndex + ": " + new String(lastBytes));
		
		final ByteBuffer inBuffer = ByteBuffer.allocate(lastBytes.length + input.length);
		inBuffer.put(lastBytes);
		inBuffer.put(input);
		inBuffer.rewind();
		
		final ByteBuffer encryptedBuffer = ByteBuffer.allocate(lastBytes.length + input.length);
				
		final Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, key, 0, lastBlockIndex);
		cipher.doFinal(inBuffer, encryptedBuffer);
		encryptedBuffer.rewind();
		
		sbc.position(lastBlockIndex * BLOCK_SIZE);
		sbc.write(encryptedBuffer);
		sbc.close();
	}

	private static void decryptBlocks(final SecretKey key)
		throws
		IOException,
		InvalidKeyException,
		NoSuchAlgorithmException,
		NoSuchPaddingException,
		InvalidAlgorithmParameterException,
		IllegalBlockSizeException,
		BadPaddingException
	{
		final SeekableByteChannel sbc = openSeekChannel();
		
				
		final int numBlocks = ((int)sbc.size() + BLOCK_SIZE -1) / BLOCK_SIZE;
		
		//read blocks from end to start to show that the decryption does not depend on previous blocks
		for(int block = numBlocks -1 ; block >= 0; block--)
		{
			final byte[] decrypted = decryptBlock(sbc, block, key);
			
			System.out.println("Decrypted block "+ block + ": \n" + new String(decrypted) + "\n");
		}
			
		sbc.close();
	}
	
	private static byte[] decryptBlock(final SeekableByteChannel sbc, final int block, final SecretKey key)
		throws
		InvalidKeyException,
		NoSuchAlgorithmException,
		NoSuchPaddingException,
		InvalidAlgorithmParameterException,
		IOException,
		IllegalBlockSizeException,
		BadPaddingException
	{
		final Cipher cipher = initCipher(Cipher.DECRYPT_MODE, key, 0, block);
		
		sbc.position(block * BLOCK_SIZE);
		
		final ByteBuffer blockBuffer = ByteBuffer.allocate(BLOCK_SIZE);
		final int readBytes = sbc.read(blockBuffer);
		
		final byte[] decrypted = cipher.doFinal(blockBuffer.array(), 0, readBytes);
				
		return decrypted;
	}

	private static SeekableByteChannel openSeekChannel()
		throws IOException
	{
		final SeekableByteChannel sbc = Files.newByteChannel(
				Paths.get("EncryptionTestFile.bin"),
				EnumSet.of(
					StandardOpenOption.CREATE,
					StandardOpenOption.READ,
					StandardOpenOption.WRITE));
		return sbc;
	}

	private static void encrypt(final SeekableByteChannel sbc, final Cipher cipher, final byte[] bytes)
		throws
		IllegalBlockSizeException,
		BadPaddingException,
		ShortBufferException,
		IOException
	{
		final ByteBuffer sourceBuffer = ByteBuffer.allocate(bytes.length).put(bytes);
		sourceBuffer.rewind();
		
		final ByteBuffer encryptedBuffer = ByteBuffer.allocate(bytes.length);
		
		cipher.doFinal(sourceBuffer, encryptedBuffer);
		encryptedBuffer.rewind();
		
		sbc.write(encryptedBuffer);

	}

	private static void decryptFull(final SeekableByteChannel sbc, final Cipher cipher)
		throws
		IOException,
		ShortBufferException,
		IllegalBlockSizeException,
		BadPaddingException
	{
		final ByteBuffer inBuffer = ByteBuffer.allocate((int)sbc.size());
		sbc.read(inBuffer);
		inBuffer.rewind();
		
		final ByteBuffer outBuffer= ByteBuffer.allocate((int)sbc.size());
		cipher.doFinal(inBuffer, outBuffer);
	
		final String decrypted = new String(outBuffer.array());
		
		System.out.println("Decrypted: \n" + decrypted + "\n");
	}

	private static Cipher initCipher(final int mode, final SecretKey key, final long ivA, final long ivB)
		throws
		InvalidKeyException,
		NoSuchAlgorithmException,
		NoSuchPaddingException,
		InvalidAlgorithmParameterException
	{
		final ByteBuffer ivBuffer = ByteBuffer.allocate(BLOCK_SIZE);
		ivBuffer.putLong(ivA);
		ivBuffer.putLong(ivB);
			
		final IvParameterSpec paramSpec = new IvParameterSpec(ivBuffer.array());
		return initCipher(mode, key, paramSpec);
	}
	
	private static Cipher initCipher(final int mode, final SecretKey key, final IvParameterSpec paramSpec)
		throws
		NoSuchAlgorithmException,
		NoSuchPaddingException,
		InvalidKeyException,
		InvalidAlgorithmParameterException
	{
		final Cipher cipher =  Cipher.getInstance(TRANSFORMATION);
		cipher.init(mode, key, paramSpec);
		return cipher;
	}

	private static SecretKey createKey(final char[] password, final byte[] salt)
		throws
		NoSuchAlgorithmException,
		InvalidKeySpecException
	{
		final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		
		//final KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		
		// Using 128Bit AES key to run this on older JAVA versions without configured longer key support.
		final KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
		final SecretKey tmp = factory.generateSecret(spec);
		final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

		System.out.println("secretKey " +
						   "algorithm: " + secret.getAlgorithm() + ", " +
						   "format   : " + secret.getFormat() + ", " +
						   "encoded length : " + secret.getEncoded().length
							);
		return secret;
	}
	


}
