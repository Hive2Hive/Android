package org.hive2hive.mobile.security;

import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.serializer.IH2HSerialize;

import java.security.Security;

/**
 * @author Nico
 */
public class SpongyCastleEncryption extends H2HDefaultEncryption {


	public SpongyCastleEncryption(IH2HSerialize serializer) {
		super(serializer, SCSecurityClassProvider.SECURITY_PROVIDER, new SCStrongAESEncryption());

		// install the SC provider instead of the BC provider
		if (Security.getProvider(SCSecurityClassProvider.SECURITY_PROVIDER) == null) {
			Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
		}
	}
}
