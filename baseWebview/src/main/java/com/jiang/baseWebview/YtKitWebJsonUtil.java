package com.jiang.baseWebview;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by xiyou on 2019/1/17
 */
public class YtKitWebJsonUtil {
    private static volatile Gson gson = null;

    private static Gson createGson() {
        GsonBuilder gsonBuilder = (new GsonBuilder()).registerTypeAdapter((new TypeToken<String>() {
        }).getType(), new TypeAdapter<String>() {
            public String read(JsonReader in) throws IOException {
                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return "";
                } else {
                    return peek == JsonToken.BOOLEAN ? Boolean.toString(in.nextBoolean()) : in.nextString();
                }
            }

            public void write(JsonWriter out, String value) throws IOException {
                out.value(value);
            }
        }).registerTypeAdapter((new TypeToken<Double>() {
        }).getType(), new TypeAdapter<Double>() {
            public void write(JsonWriter out, Double value) throws IOException {
                out.value(value);
            }

            public Double read(JsonReader in) throws IOException {
                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return 0.0D;
                } else {
                    return in.nextDouble();
                }
            }
        });
        gson = gsonBuilder.create();
        return gson;
    }

    private YtKitWebJsonUtil() {
    }

    static Gson getGson() {
        if (gson == null) {
            gson = createGson();
        }

        return gson;
    }

    static String objectToJson(Object ts) {
        String jsonStr = null;
        if (gson != null && ts != null) {
            jsonStr = gson.toJson(ts);
        }

        return jsonStr;
    }

    public static String objectToJsonDateSerializer(Object ts, final String dateformat) {
        String jsonStr = null;
        gson = (new GsonBuilder()).registerTypeHierarchyAdapter(Date.class, new JsonSerializer<Date>() {
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                SimpleDateFormat format = new SimpleDateFormat(dateformat);
                return new JsonPrimitive(format.format(src));
            }
        }).setDateFormat(dateformat).create();
        if (gson != null) {
            jsonStr = gson.toJson(ts);
        }

        return jsonStr;
    }

    public static Object jsonToBean(String jsonStr, Type type) {
        Object obj = null;
        if (gson != null) {
            try {
                obj = gson.fromJson(jsonStr, type);
            } catch (JsonSyntaxException var4) {
                ;
            }
        }

        return obj;
    }

    public static List<?> jsonToList(String jsonStr, Type type) {
        List<?> objList = null;
        if (gson != null) {
            objList = (List) gson.fromJson(jsonStr, type);
        }

        return objList;
    }

    public static Map<?, ?> jsonToMap(String jsonStr, Type type) {
        Map<?, ?> objMap = null;
        if (gson != null) {
            objMap = (Map) gson.fromJson(jsonStr, type);
        }

        return objMap;
    }

    @Nullable
    public static <T> T jsonToBean(String jsonStr, Class<T> cl) {
        T obj = null;
        if (gson != null) {
            try {
                obj = gson.fromJson(jsonStr, cl);
            } catch (JsonSyntaxException var4) {
                ;
            }
        }

        return obj;
    }

    public static <T> T jsonToBeanDateSerializer(String jsonStr, Class<T> cl, final String pattern) {
        T obj = null;
        gson = (new GsonBuilder()).registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                String dateStr = json.getAsString();

                try {
                    return format.parse(dateStr);
                } catch (ParseException var7) {
                    return null;
                }
            }
        }).setDateFormat(pattern).create();
        if (gson != null) {
            obj = gson.fromJson(jsonStr, cl);
        }

        return obj;
    }

    public static Object getJsonValue(String jsonStr, String key, Type type) {
        Object rulsObj = null;
        Map<?, ?> rulsMap = jsonToMap(jsonStr, type);
        if (rulsMap != null && rulsMap.size() > 0) {
            rulsObj = rulsMap.get(key);
        }

        return rulsObj;
    }

    static {
        gson = createGson();
    }
}
