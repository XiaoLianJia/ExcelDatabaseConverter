package com.excel.database.converter.service.impl;

import com.excel.database.converter.service.IDatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     SQlite数据库服务实现类
 * </p>
 * @author zhangbin
 * @date 2020-05-14
 */
@Slf4j
@Service("Sqlite")
public class SqliteDatabaseServiceImpl implements IDatabaseService {

    @Override
    public String createDatabase(String databasePath) throws SQLException, IOException {
        File file = new File(databasePath);
        if (! file.exists()
                && ! file.createNewFile()) {
            System.out.println("数据库文件不存在，且创建新文件失败。");
            return null;
        }

        String url = "jdbc:sqlite:" + databasePath;
        log.info("创建数据库：{}。", url);
        try (Connection connection = DriverManager.getConnection(url)) {
            DatabaseMetaData meta = connection.getMetaData();
            log.info("驱动：{}，版本：{}。", meta.getDriverName(), meta.getDriverVersion());
        }
        return url;
    }

    @Override
    public boolean dropDatabase(@NotNull String databaseUrl, String databaseName) {
        String path = databaseUrl.replace("jdbc:sqlite:", "");
        log.info("删除数据库{}。", path);
        File database = new File(path);
        return database.delete();
    }

    @Override
    public boolean crateTable(String databaseUrl, String tableName, @NotNull Map<Integer, String> fields) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, String> entry : fields.entrySet()) {
            stringBuilder.append(String.format(", [%s] VARCHAR", entry.getValue()));
        }

        String sql = String.format("CREATE TABLE IF NOT EXISTS [main].[%s]([id] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE%s);",
                tableName, stringBuilder.toString());
        log.info("创建数据表：{}。", tableName);
        log.info("SQL：{}。", sql);

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        return true;
    }

    @Override
    public boolean dropTable(String databaseUrl, String tableName) throws SQLException {
        String sql = String.format("DROP TABLE IF EXISTS [main].[%s];", tableName);
        log.info("删除数据表：{}。", tableName);
        log.info("SQL：{}。", sql);

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        return true;
    }

    @Override
    public boolean insert(String databaseUrl, String tableName,
                          @NotNull Map<Integer, String> fields, Map<Integer, String> data) throws SQLException {
        StringBuilder stringBuilderFields = new StringBuilder();
        StringBuilder stringBuilderValues = new StringBuilder();
        for (Map.Entry<Integer, String> entry : fields.entrySet()) {
            if (stringBuilderFields.toString().isEmpty()) {
                stringBuilderFields.append(String.format("[%s]", entry.getValue()));
                stringBuilderValues.append("?");
            } else {
                stringBuilderFields.append(String.format(", [%s]", entry.getValue()));
                stringBuilderValues.append(", ?");
            }
        }

        String sql = String.format("INSERT INTO [main].[%s](%s) VALUES (%s);",
                tableName, stringBuilderFields.toString(), stringBuilderValues.toString());
        log.info("插入数据表：{}。", tableName);
        log.info("SQL：{}。", sql);

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
                String value = entry.getValue();
                if (null != value
                        && value.length() > 255) {
                    log.warn("数据长度超限，{}:{}。", entry.getKey(), value);
                    value = value.substring(0, 254);
                    log.warn("数据截取，{}:{}。", entry.getKey(), value);
                }
                preparedStatement.setString(entry.getKey() + 1, entry.getValue());
            }
            preparedStatement.executeUpdate();
        }
        return true;
    }

    @Override
    public boolean insert(String databaseUrl, String tableName,
                          @NotNull Map<Integer, String> fields, List<Map<Integer, String>> list) throws SQLException {
        StringBuilder stringBuilderFields = new StringBuilder();
        StringBuilder stringBuilderValues = new StringBuilder();
        for (Map.Entry<Integer, String> entry : fields.entrySet()) {
            if (stringBuilderFields.toString().isEmpty()) {
                stringBuilderFields.append(String.format("[%s]", entry.getValue()));
                stringBuilderValues.append("?");
            } else {
                stringBuilderFields.append(String.format(", [%s]", entry.getValue()));
                stringBuilderValues.append(", ?");
            }
        }

        String sql = String.format("INSERT INTO [main].[%s](%s) VALUES (%s);",
                tableName, stringBuilderFields.toString(), stringBuilderValues.toString());
        log.info("插入数据表：{}。", tableName);
        log.info("SQL：{}。", sql);

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map<Integer, String> data : list) {
                for (Map.Entry<Integer, String> entry : data.entrySet()) {
                    preparedStatement.setString(entry.getKey() + 1, entry.getValue());
                }
                preparedStatement.executeUpdate();
            }
        }
        return true;
    }
}
