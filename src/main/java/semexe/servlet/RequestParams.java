package semexe.servlet;

import semexe.basic.OrderedStringMap;
import semexe.basic.StrUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class RequestParams extends OrderedStringMap {
    public RequestParams() {
    }

    public RequestParams(RequestParams params) {
        super(params);
    }

    public RequestParams(HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            put(key, request.getParameter(key));
        }
    }

    public String getReq(String key) throws ArgumentException {
        String val = get(key);
        if (val == null) throw ArgumentException.missing(key);
        return val;
    }

    public String[] getStringList(String key) {
        String val = get(key);
        return StrUtils.split(val, "\t");
    }

    public String[] getStringListReq(String key) throws ArgumentException {
        String val = getReq(key);
        return StrUtils.split(val, "\t");
    }

    public boolean getBoolean(String key) {
        return "true".equals(get(key));
    }

    public int getInteger(String key, int defaultVal) {
        String val = get(key);
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
