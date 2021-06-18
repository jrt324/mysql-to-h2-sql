package org.exam.service;

import com.intellij.openapi.components.ServiceManager;

import java.io.IOException;

public interface MysqlToH2Service {
    static MysqlToH2Service getInstance() {
        return ServiceManager.getService(MysqlToH2Service.class);
    }

    String convert(String mysqlTxt);

    void convert(String source, String target) throws IOException;
}
