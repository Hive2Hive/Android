package org.hive2hive.mobile.security;

import org.hive2hive.core.serializer.ISecurityClassProvider;
import org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author Nico
 */
public class SCSecurityClassProvider implements ISecurityClassProvider {

	public static final String SECURITY_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

	@Override
	public String getSecurityProvider() {
		return SECURITY_PROVIDER;
	}

	@Override
	public Class<? extends RSAPublicKey> getRSAPublicKeyClass() {
		return BCRSAPublicKey.class;
	}

	@Override
	public Class<? extends RSAPrivateKey> getRSAPrivateKeyClass() {
		return BCRSAPrivateKey.class;
	}

	@Override
	public Class<? extends RSAPrivateCrtKey> getRSAPrivateCrtKeyClass() {
		return BCRSAPrivateCrtKey.class;
	}
}
