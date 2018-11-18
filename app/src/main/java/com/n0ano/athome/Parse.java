package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Parse {

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

    String search = "<" + name;
    while (which-- > 0) {
        end = xml_get_next(search, end, resp);
        if (end < 0)
            return null;
    }
    return xml_backup(end, resp);
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
    if (c == '"') {
        while ((c = ch.next()) != '"')
            ;
    } else {
        while (number(c))
            c = ch.next();
        ch.prior();
    }
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

private String json_backup(int end, String resp)
{

    int start = end;
    if (resp.charAt(start) == '"') {
        while (resp.charAt(--start) != '"')
            ;
    } else {
        while (number(resp.charAt(start)))
            --start;
    }
    return resp.substring(start + 1, end);
}

public String json_get(String name, String resp, int which)
{
    int end = 0;

    String search = "\"" + name + "\"";
    while (which-- > 0) {
        end = json_get_next(search, end, resp);
        if (end < 0)
            return null;
    }
    return json_backup(end, resp);
}

public String json_get(String name, String resp)
{

    return json_get(name, resp, 1);
}

}
