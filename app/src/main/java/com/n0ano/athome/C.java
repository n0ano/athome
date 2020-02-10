package com.n0ano.athome;

import android.os.SystemClock;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;

//
// Created by n0ano on 11/7/18.
//
public class C {

public static final int SS_OP_INIT =    0;
public static final int SS_OP_RESET =   1;
public static final int SS_OP_BLOCK =   2;
public static final int SS_OP_UPDATE =  3;
public static final int SS_OP_STOP =    4;

public final static int BATTERY_LOW  = 20;
public final static int BATTERY_HIGH = 90;

public final static int OUTLETS_COLS = 3;

public final static String CONFIG_URI = "/cgi-bin/athome/config";
public final static String CONFIG_LOAD =  "load";                
public final static String CONFIG_SAVE =  "save";

public static boolean paused = true;

public static String suffix(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(idx + 1) : "";
}

public static int a2i(String num)
{

    return Integer.parseInt(num);
}

public static String i2a(int num)
{

    return Integer.toString(num);
}

public static String epoch_str(long epoch)
{

    Date date = new Date(epoch * 1000L);
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    format.setTimeZone(TimeZone.getDefault());
    return format.format(date);
}

public static String indent(String str)
{
    char c;
    String c1;

    boolean quote = false;
    String s = "";
    String pre = "";
    int max = str.length();
    for (int i = 0; i < max; i++) {
        if ((i < (max - 5)) && str.substring(i, i + 5).equals(" ==> ")) {
            s += " ==> \n";
            i += 4;
            continue;
        }
        if ((c = str.charAt(i)) == '"') {
            s += String.valueOf(c);
            quote = !quote;
            continue;
        } else if (quote) {
            s += String.valueOf(c);
            continue;
        }
        switch (c) {

        case '\\':
            if (str.charAt(i + 1) != 'n')
                s += "\\" + str.charAt(i + 1);
            ++i;
            break;

        case '{':
            pre += "  ";
            s += "{\n" + pre;
            break;

        case '}':
            if (pre.length() >= 2)
                pre = pre.substring(2);
            s += "}\n" + pre;
            break;

        case '[':
            pre += "  ";
            s += "[\n" + pre;
            break;

        case ']':
            if (pre.length() >= 2)
                pre = pre.substring(2);
            s += "]\n" + pre;
            break;

        case ',':
            s += ",\n" + pre;
            break;

        default:
            s += String.valueOf(c);
            break;

        }
    }
    return s;
}

public static JSONObject str2json(String str)
{
    JSONObject json;

    try {
        json = new JSONObject(str);
    } catch (Exception e) {
        Log.d("JSON parse error(" + str + ") - " + e);
        json = new JSONObject();
    }
    return json;
}

public static String json2str(JSONObject json)
{

    try {
        return json.toString();
    } catch (Exception e) {
        return "";
    }
}

public static Object json_get(JSONArray json, int i)
{

    try {
        return json.get(i);
    } catch (Exception e) {
        Log.d("JSON get error - " + e);
        return null;
    }
}

public static String encode_time(int t)
{

    if (t < 0)
        return "";
    int hr = t / 100;
    t -= hr * 100;
    return String.valueOf(hr) + ":" + String.format("%02d", t);
}

public static int decode_time(String t)
{

    if (t.isEmpty())
        return -1;
    int idx = t.indexOf(":");
    if (idx < 0)
        return Integer.parseInt(t);
    else
        return (Integer.parseInt(t.substring(0, idx)) * 100) + Integer.parseInt(t.substring(idx + 1));
}

public static String get_cfg(int indent)
{

    P.put("ss:enable", P_SS.get("ss_enable", false));
    P.put("ss:start", P_SS.get("ss_start", 0));
    P.put("ss:delay", P_SS.get("ss_delay", 0));
    P.put("ss:fade", P_SS.get("ss_fade", 0));
    return P.get_cfg(indent);
}

public static void new_cfg(String cfg)
{

    P.new_cfg(cfg);
    P_SS.put("ss_enable", P.get_boolean("ss:enable"));
    P_SS.put("ss_start", P.get_int("ss:start"));
    P_SS.put("ss_delay", P.get_int("ss:delay"));
    P_SS.put("ss_fade", P.get_int("ss:fade"));
}

public static void remote_line(String url, String params, String line)
{
    URL server;
    InputStreamReader in_rdr;
    BufferedReader inp;

    if (url.isEmpty())
        return;

    url = url + "?" + params + URLEncoder.encode(line);
	try {
        server = new URL(url);
        HttpURLConnection url_con = (HttpURLConnection) server.openConnection();
        in_rdr = new InputStreamReader(url_con.getInputStream());
        inp = new BufferedReader(in_rdr);
	} catch (Exception e) {
		Log.d("DDD", "remote_log failed" + e);
        return;
	}
    try {
        inp.close();
    } catch (Exception e) {
		Log.d("DDD", "remote_log close failed" + e);
    }
}

public static void remote_log(String url, String params, String line)
{

    if (P.get_int("debug") <= 0)
        return;
    int len = 80;
    String pre = "";
    line = line.replace("\n", "\\n");

    int max = line.length();
    int maxl = P.get_int("general:log_length");
    if ((maxl > 0) && (max > maxl)) {
        line = line.substring(0, maxl);
        max = line.length();
    }

    while (max > len) {
        remote_line(url, params, pre + line.substring(0, len));
        line = line.substring(len);
        max -= len;
        pre = "  ";
        len = 80 - 2;
    }
    if (max > 0)
        remote_line(url, params, pre + line);
}

public static String resp_header(String key, HttpURLConnection con)
{
    int i;
    String body;
    String k;

    i = 0;
    for (;;) {
        if ((body = con.getHeaderField(i)) == null)
            return "";
        k = con.getHeaderFieldKey(i);
        if (k.equals(key))
            return body;
        ++i;
    }   
}

public static HashMap<String, String> parse_auth(String str)
{

    HashMap<String, String> auth = new HashMap<String, String>();
    String[] parts = str.split(", ");
    for (int i = 0; i < parts.length; i++) {
        int idx = parts[i].indexOf("=");
        if (idx < 0)
            continue;
        auth.put(parts[i].substring(0, idx), parts[i].substring(idx + 2, parts[i].length() - 1));
    }
    return auth;
}

private static String mk_cnonce()
{
    String s = "";

    for (int i = 0; i < 8; i++)
        s += Integer.toHexString(new Random().nextInt(16));
    return s;
}

private static final String HEX_LOOKUP = "0123456789abcdef";

private static String to_hex(byte[] bytes)
{
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for(int i = 0; i < bytes.length; i++){
        sb.append(HEX_LOOKUP.charAt((bytes[i] & 0xF0) >> 4));
        sb.append(HEX_LOOKUP.charAt((bytes[i] & 0x0F) >> 0));
    }
    return sb.toString();
}

private static String mk_md5(String str)
{
    String r = null;
    MessageDigest md5;

    try {
        md5 = MessageDigest.getInstance("MD5");
    } catch (Exception e) {
        Log.d("DDD", "md5 algorithm missing");
        return r;
    }

    try {
        md5.reset();
        md5.update(str.getBytes("ISO-8859-1"));
        r = to_hex(md5.digest());
    } catch (Exception e) {
        Log.d("DDD", "mk_md5 failed");
    }
    return r;
}

//  mk_digest - create a digest authentication string
//
//  Note: This code makes some assumptions about the
//    challenge string received from the HTTP server, e.g.
//
//      alorithm - MD5 (default if unspecified)
//      qop - auth or auth-int
//      opaque - not provided
//
//    if the eGauge server changes these assumptions then
//    this code will have to change
//
public static String mk_digest(String header, String uri, String usr, String pwd)
{
    String digest = null;

    HashMap<String, String> auth = parse_auth(header);

    String nonce = auth.get("nonce");
    String realm = auth.get("Digest realm");
    String qop = auth.get("qop");
    String cnonce = mk_cnonce();
    String nc = "1";
    String ha1 = mk_md5(usr + ":" + realm + ":" + pwd);
    String ha2 = mk_md5("GET" + ":" + uri);
    String response = null;
    if (!ha1.isEmpty() && !ha2.isEmpty())
        response = mk_md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);

    if (response != null) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Digest ");
        sb.append("username").append("=\"").append(usr).append("\", ");
        sb.append("realm").append("=\"").append(realm).append("\", ");
        sb.append("nonce").append("=\"").append(nonce).append("\", ");
        sb.append("uri").append("=\"").append(uri).append("\", ");
        sb.append("response").append("=\"").append(response).append("\", ");
        sb.append("qop").append("=").append(qop).append(", ");
        sb.append("nc").append("=").append(nc).append(", ");
        sb.append("cnonce").append("=\"").append(cnonce).append("\"");
        digest = sb.toString();
    }
    return digest;
}

}
