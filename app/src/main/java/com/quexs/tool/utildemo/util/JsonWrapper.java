package com.quexs.tool.utildemo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonWrapper {
    private Gson gson;
    private Gson gsonBuilder;

    /**
     *
     * @param obj
     * @param isBuilder 启用特殊配置
     * @return
     */
    public String toJson(Object obj, boolean isBuilder){
        return getGson(isBuilder).toJson(obj);
    }

    /**
     * 此方法将指定对象（包括泛型类型的对象）序列化为其对象json
     * @param obj
     * @param typeOfSrc typeOfSrc = new TypeToken<Object>(){}.getType();
     * @param isBuilder 启用特殊配置
     * @return
     */
    public String toJson(Object obj, Type typeOfSrc, boolean isBuilder){
        return getGson(isBuilder).toJson(obj,typeOfSrc);
    }

    /**
     *
     * @param json
     * @param classOfT
     * @param isBuilder 启用特殊配置
     * @param <T>
     * @return
     */
    public <T> T fromJson(String json, Class<T> classOfT, boolean isBuilder){
        return getGson(isBuilder).fromJson(json, classOfT);
    }

    /**
     *
     * @param json
     * @param typeOfT ype typeOfSrc = new TypeToken<Object>(){}.getType();
     * @param isBuilder 启用特殊配置
     * @param <T>
     * @return
     */
    public <T> T fromJson(String json, Type typeOfT, boolean isBuilder){
        return getGson(isBuilder).fromJson(json, typeOfT);
    }

    public Gson getGson(boolean isBuilder){
        return isBuilder ? getGsonBuilder() : getGson();
    }

    private Gson getGsonBuilder(){
        if(gsonBuilder == null){
            gsonBuilder = new GsonBuilder()
                    .serializeNulls() //空参参与转化成字符串
                    .excludeFieldsWithoutExposeAnnotation() //不对没有用@Expose注解的属性进行操作
                    .enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
                    .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS") //时间转化为特定格式
                    .setPrettyPrinting() //对结果进行格式化，增加换行
                    .disableHtmlEscaping() //防止特殊字符出现乱码
                    .create();
        }
        return gsonBuilder;
    }

    private Gson getGson(){
        if(gson == null){
            gson = new Gson();
        }
        return gson;
    }

}
