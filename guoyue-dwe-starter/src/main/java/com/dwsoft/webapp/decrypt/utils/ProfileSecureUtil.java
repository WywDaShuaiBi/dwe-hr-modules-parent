package com.dwsoft.webapp.decrypt.utils;

import java.io.UnsupportedEncodingException;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.SecureUtil;


public class ProfileSecureUtil {

	private final static String key = "tXPoFtBUZjQBJkBfMRHLWTrihRgjfiPO";

	public static String encrypt(String content) throws UnsupportedEncodingException {
		if (content == null) {
			return null;
		}
		byte[] encrypt = SecureUtil.aes(key.getBytes()).encrypt(content.getBytes("utf-8"));
		return Base64Encoder.encode(encrypt);
	}

	public static String decrypt(String content) {
		if (content == null) {
			return null;
		}
		byte[] decrypt = SecureUtil.aes(key.getBytes()).decrypt(Base64Decoder.decode(content));
		return new String(decrypt);
	}

	private static String getRandStr(int num) {
		String strs = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
		StringBuffer buff = new StringBuffer();
		for (int i = 1; i <= num; i++) {
			char str = strs.charAt((int) (Math.random() * 52));
			buff.append(str);
		}
		return buff.toString();
	}

	public static void main(String[] args) throws Exception {
		String encrypt = encrypt(
				"root");
		System.out.println(encrypt);
		String decrypt = decrypt(encrypt);
		System.out.println(decrypt);

	}
}
