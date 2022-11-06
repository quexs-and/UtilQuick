package com.quexs.tool.utillib.util;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 加密
 */
public class AESEncrypt {

    //加密算法
    private final String keyAlgorithm;
    //字符编码
    private final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;;

    public AESEncrypt(){
        this("AES");
    }

    /**
     * 加密算法
     * @param keyAlgorithm
     */
    public AESEncrypt(String keyAlgorithm){
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * AES 加密
     *
     * @param data      待加密内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    public  String encrypt(String data, String secretKey) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance("AES/PKCS5Padding");
            //初始化为加密密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey));
            byte[] encryptByte = cipher.doFinal(data.getBytes(CHARSET_UTF8));
            // 将加密以后的数据进行 Base64 编码
            return base64Encode(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     */
    public String decrypt(String base64Data, String secretKey) {
        try {
            byte[] data = base64Decode(base64Data);
            Cipher cipher = Cipher.getInstance(keyAlgorithm);
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey));
            //执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, CHARSET_UTF8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用密码获取 AES 秘钥
     */
    public SecretKeySpec getSecretKey(String secretKey) {
        secretKey = toMakeKey(secretKey, 32, "0");
        return new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), keyAlgorithm);
    }

    /**
     * 如果 AES 的密钥小于 {@code length} 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param length    密钥应有的长度
     * @param text      默认补的文本
     * @return 密钥
     */
    private String toMakeKey(String secretKey, int length, String text) {
        // 获取密钥长度
        int strLen = secretKey.length();
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            StringBuilder builder = new StringBuilder();
            // 将key添加至builder中
            builder.append(secretKey);
            // 遍历添加默认文本
            for (int i = 0; i < length - strLen; i++) {
                builder.append(text);
            }
            // 赋值
            secretKey = builder.toString();
        }
        return secretKey;
    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    public byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.NO_WRAP);
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     */
    public String base64Encode(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }


}
