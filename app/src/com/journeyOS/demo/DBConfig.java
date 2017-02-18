package com.journeyOS.demo;

import android.net.Uri;

public class DBConfig {

    public static final int SCHEMA_VERSION = 1;

    public static final String AUTHORITIES = "com.journeyOS.liteprovider";

    public static final Uri URL = Uri.parse("content://" + AUTHORITIES + "/" + "test");

    //Column
    public static final String KEY = "key";
    public static final String VALUE = "value";
}
