package org.hive2hive.mobile.security;

import org.hive2hive.core.security.IStrongAESEncryption;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

/**
 * Copy of {@link org.hive2hive.core.security.BCStrongAESEncryption}, but using spongy castle
 *
 * @author Nico
 */
public class SCStrongAESEncryption implements IStrongAESEncryption {

	@Override
	public byte[] encryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException {
		try {
			return processAESCipher(true, data, key, initVector);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			throw new GeneralSecurityException("Cannot encrypt the data with AES 256bit", e);
		}
	}

	@Override
	public byte[] decryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException {
		try {
			return processAESCipher(false, data, key, initVector);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			throw new GeneralSecurityException("Cannot decrypt the data with AES 256bit", e);
		}
	}

	private static byte[] processAESCipher(boolean encrypt, byte[] data, SecretKey key, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		// seat up engine, block cipher mode and padding
		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

		// apply parameters
		CipherParameters parameters = new ParametersWithIV(new KeyParameter(key.getEncoded()), initVector);
		cipher.init(encrypt, parameters);

		// process ciphering
		byte[] output = new byte[cipher.getOutputSize(data.length)];

		int bytesProcessed1 = cipher.processBytes(data, 0, data.length, output, 0);
		int bytesProcessed2 = cipher.doFinal(output, bytesProcessed1);
		byte[] result = new byte[bytesProcessed1 + bytesProcessed2];
		System.arraycopy(output, 0, result, 0, result.length);
		return result;
	}
}
