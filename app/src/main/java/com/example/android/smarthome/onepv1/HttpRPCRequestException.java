package com.example.android.smarthome.onepv1;

/*=============================================================================
* HttpRPCRequestException.java
* Exception class for http request failure.
*==============================================================================
*
* Tested with JDK 1.6
*
* Copyright (c) 2011, Exosite LLC
* All rights reserved.
*/

@SuppressWarnings("serial")
public class HttpRPCRequestException extends Exception {

    public HttpRPCRequestException(final String message) {
        super(message);
    }
}