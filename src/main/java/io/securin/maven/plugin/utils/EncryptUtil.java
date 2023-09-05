package io.securin.maven.plugin.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncryptUtil {

	private static final String AES = "AES/CBC/PKCS5Padding";

	public String decryptV1(String str, Map<String, String> encKyMap) {
		try {
			Cipher c = Cipher.getInstance(AES); // NOSONAR
			SecretKeySpec sk = new SecretKeySpec(DatatypeConverter.parseHexBinary(encKyMap.get("KEY")), "AES");
			IvParameterSpec iv = new IvParameterSpec(DatatypeConverter.parseHexBinary(encKyMap.get("IV"))); // NOSONAR
			c.init(Cipher.DECRYPT_MODE, sk, iv);
			byte[] fnlBytes = c.doFinal(Base64.getDecoder().decode(str.getBytes()));
			return new String(fnlBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		}

	}

	public static EncryptUtil getInstance() {
		return EncryptUtilHelper.INSTANCE;
	}

	private static class EncryptUtilHelper {
		private static final EncryptUtil INSTANCE = new EncryptUtil();
	}

}
