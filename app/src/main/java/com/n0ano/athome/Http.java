package com.n0ano.athome;

import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http
{

public class R
{

int code;
String body;

public R(int code, String body)
{

    this.code = code;
    this.body = body;
}
}

public final static int OK =        HttpURLConnection.HTTP_OK;
public final static int ERR =       HttpURLConnection.HTTP_INTERNAL_ERROR;
public final static int AUTH =      HttpURLConnection.HTTP_UNAUTHORIZED;
public final static int TIMEOUT =   HttpURLConnection.HTTP_CLIENT_TIMEOUT;

public Http()
{
}

/*
 *  call an HTTP(S) uri to get a response.  Note that this will return
 *      a string with the `entire` response.  If the API returns a lot
 *      of data you might be better off using the open_url/read_url/close_url
 *      interface available right below this one.
 */
public R call_api_nolog(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;
    InputStreamReader in = null;
    BufferedReader inp = null;
    String res = "";
    R r = new R(OK, "");

    if (!params.equals(""))
        uri = uri + "?" + params;
    try {
        URL url = new URL(uri);
        con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        if (type.equals("POST"))
            con.setDoOutput(true);
        if (!auth.isEmpty())
            con.setRequestProperty("Authorization", auth);
        if (body != null) {
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoInput(true);
            OutputStream os = con.getOutputStream();
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            wr.write(body);
            wr.flush();
            wr.close();
        }
    } catch (Exception e) {
        r.code = ERR;
        r.body = e.toString();
        if (con != null)
            con.disconnect();
        return r;
    }

    try {
        r.code = con.getResponseCode();
        if (r.code == AUTH) {
            r.body = C.resp_header("WWW-Authenticate", con);
            return r;
        } else if (r.code != OK) {
            r.body = "HTTP error";
            return r;
        }
    } catch (Exception e) {
        con.disconnect();
        r.body = e.toString();
        return r;
    }

    try {
        in = new InputStreamReader(con.getInputStream());
        inp = new BufferedReader (in);
        StringBuilder response = new StringBuilder();
        for (String line; (line = inp.readLine()) != null; )
            response.append(line).append('\n');
        r.body = response.toString();
    } catch (java.net.SocketTimeoutException e) {
        r.code = TIMEOUT;
        r.body = e.toString();
    } catch (Exception e) {
        r.code = ERR;
        r.body = e.toString();
    } finally {
        con.disconnect();
    }
    return r;
}

/*
 *  call an HTTP(S) uri to get a response and log the request & response
 */
public R call_api(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;
    String line;
    long stime, etime;

    stime = SystemClock.elapsedRealtime();
    R res = call_api_nolog(type, uri, params, auth, body);
    etime = SystemClock.elapsedRealtime();
    line = type + "(" + (etime - stime) + "):" + uri;
    if (!params.isEmpty())
        line += "?" + params;
    if (!auth.isEmpty())
        line += ", auth - '" + auth + "'";
    if (body != null)
        line += ", body - " + body;
    line += " ==> " + res.code + ":" + res.body;
    Log.s(line);
    return res;
}

public R call_api(String uri, String params)
{

    return call_api("GET", uri, params, "", null);
}

public BufferedReader open_url(String url, String auth)
{
    URL server;
    InputStreamReader in_rdr;
    BufferedReader inp;

    try {
        Log.d("get data from " + url);
        server = new URL(url);
        HttpURLConnection url_con = (HttpURLConnection) server.openConnection();
        in_rdr = new InputStreamReader(url_con.getInputStream());
        inp = new BufferedReader (in_rdr);
    } catch (Exception e) {
        Log.d("open_url failed - " + e);
        return null;
    }
    return inp;
}

public String read_url(BufferedReader inp)
{
    String line = null;

    try {
        line = inp.readLine();
    } catch (Exception e) {
        Log.d("read_url failed - " + e);
        return null;
    }
    return line;
}

public void close_url(BufferedReader inp)
{

    try {
        inp.close();
    } catch (Exception e) {
        Log.d("close_url failed - " + e);
    }
}

}
