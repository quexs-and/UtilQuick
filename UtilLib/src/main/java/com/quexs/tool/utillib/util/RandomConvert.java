package com.quexs.tool.utillib.util;

import java.util.Random;

/**
 * 随机数转换
 */
public class RandomConvert {

    public RandomConvert() {

    }

    /**
     * 生成一个0 到 count 之间的随机数(不包含endNum的随机数)
     *
     * @param endNum
     * @return
     */
    public int getNum(int endNum) {
        if (endNum > 0) {
            Random random = new Random();
            return random.nextInt(endNum);
        }
        return -1;
    }

    /**
     * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
     *
     * @param startNum
     * @param endNum
     * @return
     */
    public int getNum(int startNum, int endNum) {
        if (endNum > startNum) {
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum;
        }
        return -1;
    }

    /**
     * 生成随机大写字母
     *
     * @return
     */
    public String getLargeLetter() {
        Random random = new Random();
        return String.valueOf((char) (random.nextInt(25) + 'A'));
    }

    /**
     * 生成随机大写字母字符串
     *
     * @return
     */
    public String getLargeLetter(int size) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            buffer.append((char) (random.nextInt(25) + 'A'));
        }
        return buffer.toString();
    }

    /**
     * 生成随机小写字母
     *
     * @return
     */
    public String getSmallLetter() {
        Random random = new Random();
        return String.valueOf((char) (random.nextInt(25) + 'a'));
    }

    /**
     * 生成随机小写字母字符串
     *
     * @return
     */
    public String getSmallLetter(int size) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            buffer.append((char) (random.nextInt(25) + 'a'));
        }
        return buffer.toString();
    }

    /**
     * 数字与小写字母混编字符串
     *
     * @param size
     * @return
     */
    public String getNumSmallLetter(int size) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            if (random.nextInt(2) % 2 == 0) {//字母
                buffer.append((char) (random.nextInt(25) + 'a'));
            } else {//数字
                buffer.append(random.nextInt(10));
            }
        }
        return buffer.toString();
    }

    /**
     * 数字与大写字母混编字符串
     *
     * @param size
     * @return
     */
    public String getNumLargeLetter(int size) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            if (random.nextInt(2) % 2 == 0) {//字母
                buffer.append((char) (random.nextInt(25) + 'A'));
            } else {//数字
                buffer.append(random.nextInt(10));
            }
        }
        return buffer.toString();
    }

    /**
     * 数字与大小写字母混编字符串
     *
     * @param size
     * @return
     */
    public String getNumLargeSmallLetter(int size) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            if (random.nextInt(2) % 2 == 0) {
                //字母
                if (random.nextInt(2) % 2 == 0) {
                    buffer.append((char) (random.nextInt(25) + 'A'));
                } else {
                    buffer.append((char) (random.nextInt(25) + 'a'));
                }
            } else {
                //数字
                buffer.append(random.nextInt(10));
            }
        }
        return buffer.toString();
    }
}
