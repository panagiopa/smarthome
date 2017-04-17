package com.example.android.smarthome.onepv1;

/*=============================================================================
* HttpRPCResponseException.java
* Exception class for http response failure.
*==============================================================================
*
* Tested with JDK 1.6
*
* Copyright (c) 2011, Exosite LLC
* All rights reserved.
*/

@SuppressWarnings("serial")
public class HttpRPCResponseException extends Exception {

    public HttpRPCResponseException(final String message) {
        super(message);
    }
}