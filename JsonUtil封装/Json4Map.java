package cn.proflu.profluweb.common.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;

public class Json4Map {
    public static Map<String, Object> parseJSON2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        JSONObject json = JSONObject.fromObject(jsonStr);
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<JSONObject> it = ((JSONArray) v).iterator();
                while (it.hasNext()) {
                    JSONObject json2 = it.next();
                    list.add(parseJSON2Map(json2.toString()));
                }
                map.put(k.toString(), list);
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }

    public static List getMainInfo(String result) {
        Map<String, Object> map = parseJSON2Map(result);
        Map d = (Map) map.get("data");
        if (d != null) {
            List list = (List) d.get("items");
            if (list != null) {
                return list;
            }
        }
        return null;
    }
    public static String getNewsInfo(String result) {
        Map<String, Object> map = parseJSON2Map(result);
        Map d = (Map) map.get("article");
        String  title = (String) d.get("title");
        return title;
    }
}
