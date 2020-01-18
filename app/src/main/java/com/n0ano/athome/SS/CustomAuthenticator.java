package com.n0ano.athome.SS;

import com.n0ano.athome.Log;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class CustomAuthenticator extends Authenticator
{

private String user;
private String pass;

public CustomAuthenticator(String user, String pass)
{

    this.user = user;
    this.pass = pass;
}

@Override
protected PasswordAuthentication getPasswordAuthentication()
{

    return new PasswordAuthentication(user, pass.toCharArray());
}

}
