package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Char
{

private String line;
private int idx;
String ws;

public Char(String line, int start, String ws)
{

    this.line = line;
    this.idx = start;
    this.ws = ws;
}

public Char(String line, int start)
{

    this.line = line;
    this.idx = start;
    this.ws = null;
}

private boolean white(int c)
{
    int ch = 0;

    for (int i = 0; i < ws.length(); i++)
        ch = ws.charAt(i);
        if (c == ch)
            return true;
    return false;
}

public int next()
{

    if (ws != null) {
        while (white(next()))
            ;
        prior();
    }
    return line.charAt(++idx);
}

public int prior()
{

    if (--idx < 0)
        idx = 0;
    return line.charAt(idx);
}

public int peek()
{

    next();
    return prior();
}

public int index()
{

    return idx;
}

}
