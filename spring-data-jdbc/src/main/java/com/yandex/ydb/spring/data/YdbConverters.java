package com.yandex.ydb.spring.data;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

public class YdbConverters {


    private YdbConverters() {
        //
    }

    @ReadingConverter
    public enum StringToBytesConverter implements Converter<String, byte[]> {
        INSTANCE;

        @Override
        public byte[] convert(String source) {
            return source == null ? null : source.getBytes();
        }
    }
}
