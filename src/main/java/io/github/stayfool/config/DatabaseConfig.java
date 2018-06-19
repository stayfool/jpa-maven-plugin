package io.github.stayfool.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author paoding
 * @date 2017/12/7
 */
@Data
public class DatabaseConfig {

    @Parameter
    private String type = "mysql";

    @Parameter
    private String tablePrefix;

    @Parameter
    private List<String> includes;

    @Parameter
    private List<String> excludes;

    @Parameter
    private Properties properties;

    @Parameter(required = true)
    private DatasourceConfig datasource;

    public DatabaseProperties initDatabaseProperties() {
        properties = properties == null ? new Properties() : properties;

        loadDefaultProperties();

        return DatabaseProperties.builder()
                .columnGenerator(properties.getProperty("column.generator"))
                .columnKey(properties.getProperty("column.key"))
                .columnKeyValue(properties.getProperty("column.key.value"))
                .columnName(properties.getProperty("column.name"))
                .columnType(properties.getProperty("column.type"))
                .tableComment(properties.getProperty("table.comment"))
                .tableFieldSql(properties.getProperty("table.field.sql"))
                .tableInfoSql(properties.getProperty("table.info.sql"))
                .tableName(properties.getProperty("table.name"))
                .convertMap(loadTypeConvertMap())
                .build();
    }

    private Map<Pattern, String> loadTypeConvertMap() {
        Map<Pattern, String> convertMap = new HashMap<>(16);

        properties.forEach((k, v) -> {
            String javaType = (String) k;
            if (StringUtils.isNotBlank((javaType = getJavaType(javaType)))) {
                convertMap.put(Pattern.compile((String) v), javaType);
            }
        });
        return convertMap;
    }

    private void loadDefaultProperties() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(type);
            bundle.keySet().forEach(key -> properties.putIfAbsent(key, bundle.getString(key)));
        } catch (Exception e) {
            System.out.println("default database properties file does not exists, ignore");
        }
    }

    private String getJavaType(String string) {
        if (string.startsWith("java.type.")) {
            return string.substring(10);
        }
        return "";
    }
}
