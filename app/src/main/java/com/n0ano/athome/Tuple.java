package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Tuple<T>
{

private final T pair_first;
private final T pair_second;

public Tuple(T first, T second) {
    pair_first = first;
    pair_second = second;
}

public T first() {
    return pair_first;
}

public T second() {
    return pair_second;
}

}
