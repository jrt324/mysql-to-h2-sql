package org.exam.service.impl;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.exam.service.MysqlToH2Service;
import org.exam.util.FileUtils;
import com.alibaba.druid.sql.dialect.mysql.visitor.MysqlToH2Visitor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MysqlToH2ServiceImpl implements MysqlToH2Service {
    public static InputStream readFile(String filepath) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(filepath));
        PushbackInputStream pb = new PushbackInputStream(input, 2);
        byte[] magicBytes = new byte[2];
        if (pb.read(magicBytes) != 2) {
            throw new RuntimeException("读取文件出错[" + filepath + "]");
        }
        pb.unread(magicBytes);
        ByteBuffer bb = ByteBuffer.wrap(magicBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        short magic = bb.getShort();
        return magic == (short) GZIPInputStream.GZIP_MAGIC ? new GZIPInputStream(pb) : pb;
    }

    @Override
    public String convert(String mysqlTxt) {
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(mysqlTxt, JdbcConstants.MYSQL);
        StringBuilder sb = new StringBuilder();
        MysqlToH2Visitor visitor = new MysqlToH2Visitor(sb);
        for (SQLStatement statement : sqlStatements) {
            statement.accept(visitor);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void convert(String source, String target) throws IOException {
        try (InputStream fileIn = readFile(source); FileOutputStream fileOut = new FileOutputStream(target)) {
            String mysqlText = FileUtils.copyToString(fileIn, StandardCharsets.UTF_8);
            fileOut.write(convert(mysqlText).getBytes(StandardCharsets.UTF_8));
        }
    }
}
