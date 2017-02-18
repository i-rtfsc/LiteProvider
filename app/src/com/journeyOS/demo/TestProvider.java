package com.journeyOS.demo;

import com.journeyOS.liteprovider.Column;
import com.journeyOS.liteprovider.LiteProvider;
import com.journeyOS.liteprovider.Table;

public class TestProvider extends LiteProvider {

    @Override
    protected String getAuthority() {
        return DBConfig.AUTHORITIES;
    }

    @Override
    protected int getSchemaVersion() {
        return DBConfig.SCHEMA_VERSION;
    }

    @Table
    public class Test {
        @Column(Column.FieldType.TEXT)
        public static final String KEY = DBConfig.KEY;

        @Column(Column.FieldType.TEXT)
        public static final String VALUE = DBConfig.VALUE;

    }

}
