package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Parse {

private String stoi(int precision, String val)
{

    if (precision > 0) {
        int l = val.length();
        return val.substring(0, l - precision) + "." + val.substring(l - precision);
    } else
        return val;
}

public int xml_get_next(String search, int start, String resp)
{

    while (start >= 0) {
        start = resp.indexOf(search, start);
        if (start < 0)
            return -1;
        start += search.length();
        if (resp.charAt(start) == '>' || resp.charAt(start) == ' ')
            break;
    }
    Char ch = new Char(resp, start);
    while (ch.next() != '"')
        ;
    start = ch.index();
    while (ch.next() != '"')
        ;
    return ch.index();
}

private String xml_backup(int end, String resp)
{

    int start = end - 1;
    while (resp.charAt(start) != '"')
        --start;
    return resp.substring(start + 1, end);
}

public String xml_get(String name, String resp, int which)
{
    int end = 0;
    int precision = 0;

    if (name.charAt(0) == '%') {
        precision = name.charAt(1) - '0';
        name = name.substring(3);
    }
    String search = "<" + name;
    while (which-- > 0) {
        end = xml_get_next(search, end, resp);
        if (end < 0)
            return null;
    }
    return stoi(precision, xml_backup(end, resp));
}

public String xml_get(String name, String resp)
{

    return xml_get(name, resp, 1);
}

public int json_get_next(String search, int start, String resp)
{
    int c;

    start = resp.indexOf(search, start);
    if (start < 0)
        return start;
    start += search.length();
    if ((start = resp.indexOf(":", start)) < 0)
        return -1;
    Char ch = new Char(resp, start + 1);
    while ((c = ch.peek()) == ' ' || c == '\t' || c == '\n')
        c = ch.next();
    return ch.index();
}

private boolean number(int c)
{

    switch (c) {

    case '.':   case ',':
    case '0':   case '1':   case '2':   case '3':   case '4':
    case '5':   case '6':   case '7':   case '8':   case '9':
        return true;

    default:
        return false;

    }
}

private int json_span(int start, Char ch)
{
    int c;

    while ((c = ch.peek()) != start)
        ch.next();
    return ch.index();
}

public String json_list(String resp, int idx)
{
    String r;
    int c;
    int start;

    Char ch = new Char(resp, 0);
    while (--idx > 0) {
        while ((c = ch.next()) != ',')
            if (c == Char.EOF)
                return null;
    }
    while (ch.peek() != '"')
        if ((c = ch.next()) == Char.EOF)
            return null;
    ch.next();
    start = ch.index();
    r = resp.substring(start, json_span('"', ch));
    return r;
}

private String json_object(int start, String resp)
{
    int c;
    int end;

    Char ch = new Char(resp, start);
    switch (ch.peek()) {

    case '[':
        ch.next();
        start = ch.index();
        end = json_span(']', ch);
        break;

    case '"':
        ch.next();
        start = ch.index();
        end = json_span('"', ch);
        break;

    default:
        while ((c = ch.next()) >= '0' && c <= '9')
            ;
        end = ch.index();
        break;

    }
    return resp.substring(start, end);
}

public String json_get(String name, String resp, int which)
{
    String r;
    int start = 0;
    int precision = 0;

    if (name.charAt(0) == '%') {
        precision = name.charAt(1) - '0';
        name = name.substring(3);
    }
    String search = "\"" + name + "\"";
    while (which-- > 0) {
        start = json_get_next(search, start, resp);
        if (start < 0)
            return null;
    }
    r = stoi(precision, json_object(start, resp));
    return r;
}

public String json_get(String name, String resp)
{

    return json_get(name, resp, 1);
}

}
