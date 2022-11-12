package com.quexs.tool.utillib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAEncrypt {

    //加密算法
    private final String RSA = "RSA";

    public RSAEncrypt(){

    }

    /**
     * 随机生成RSA密钥对
     *
     * @param keyLength 密钥长度，范围：512～2048<br>
     *                  一般1024
     * @return
     */
    public KeyPair generateRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(keyLength);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用公钥加密 <br>
     * 每次加密的字节数，不能超过密钥的长度值减去11
     *
     * @param data 需加密数据的byte数据
     * @param publicKey 公钥
     * @return 加密后的byte型数据
     */
    public byte[] encrypt(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            // 传入编码数据并返回编码结果
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用私钥解密
     *
     * @param encryptedData 经过encryptedData()加密返回的byte数据
     * @param privateKey    私钥
     * @return
     */
    public byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 公钥字符串还原成公钥
     * @param publicKeyStr 公钥数据字符串
     * @return
     * @throws Exception
     */
    public RSAPublicKey convertPublicKey(String publicKeyStr) throws Exception {
        return convertPublicKey(decode(publicKeyStr));
    }

    /**
     * 私钥数据还原私钥
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public RSAPrivateKey convertPrivateKey(String privateKeyStr) throws Exception {
        return convertPrivateKey(decode(privateKeyStr));
    }

    /**
     * 公钥还原
     * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
     * @param publicKeyEncoded
     * @return
     * @throws Exception
     */
    public RSAPublicKey convertPublicKey(byte[] publicKeyEncoded) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return (RSAPublicKey)keyFactory.generatePublic(keySpec);
    }

    /**
     * 私钥还原
     * @param privateKeyEncoded
     * @return
     * @throws Exception
     */
    public RSAPrivateKey convertPrivateKey(byte[] privateKeyEncoded) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用N、e值还原公钥
     *
     * @param modulus
     * @param publicExponent
     * @return
     * @throws Exception
     */
    public RSAPublicKey convertPublicKey(String modulus, String publicExponent) throws Exception {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(publicExponent);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return (RSAPublicKey)keyFactory.generatePublic(keySpec);
    }

    /**
     * 使用N、d值还原私钥
     *
     * @param modulus
     * @param privateExponent
     * @return
     * @throws Exception
     */
    public RSAPrivateKey convertPrivateKey(String modulus, String privateExponent) throws Exception {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
    }

    /**
     * 从文件流中还原私钥
     * @param in
     * @return
     * @throws IOException
     */
    public RSAPrivateKey convertPrivateKey(InputStream in) throws Exception {
        return convertPrivateKey(readKey(in));
    }

    /**
     * 从文件流中还原公钥
     * @param in
     * @return
     * @throws Exception
     */
    public RSAPublicKey convertPublicKey(InputStream in) throws Exception {
        return convertPublicKey(readKey(in));
    }

    /**
     *
     * @param in
     * @return
     * @throws IOException
     */
    private String readKey(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            }
            sb.append(readLine);
            sb.append('\r');
        }
        br.close();
        return sb.toString();
    }

    /**
     * 编码
     *
     * @param data
     * @return
     */
    public String encode(byte[] data) {
        char[] base64EncodeChars = new char[]
                {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
                        '6', '7', '8', '9', '+', '/'};

        StringBuilder sb = new StringBuilder();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    /**
     * 解码
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    private byte[] decode(String str) throws UnsupportedEncodingException {
        byte[] base64DecodeChars = new byte[]
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
                        54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                        12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29,
                        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1,
                        -1, -1, -1};

        StringBuilder sb = new StringBuilder();
        byte[] data;
        data = str.getBytes(StandardCharsets.US_ASCII);
        int len = data.length;
        int i = 0;
        int b1, b2, b3, b4;
        while (i < len) {
            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1)
                break;

            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1)
                break;
            sb.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            do {
                b3 = data[i++];
                if (b3 == 61)
                    return sb.toString().getBytes("iso8859-1");
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1)
                break;
            sb.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            do {
                b4 = data[i++];
                if (b4 == 61)
                    return sb.toString().getBytes("iso8859-1");
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1)
                break;
            sb.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return sb.toString().getBytes("iso8859-1");
    }

}
