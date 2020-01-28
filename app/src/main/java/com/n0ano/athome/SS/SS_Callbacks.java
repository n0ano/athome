package com.n0ano.athome.SS;

public interface SS_Callbacks
{

    ScreenInfo ss_start();
    void ss_stop();
    void ss_toolbar(String from, int mode);
    void ss_inited();
}
