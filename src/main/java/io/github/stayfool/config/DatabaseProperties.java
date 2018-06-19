package io.github.stayfool.config;

import io.github.stayfool.util.Constant;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author stay fool
 * @date 2017/8/15
 */
@Builder
@Data
public class DatabaseProperties {
    private String columnKeyValue;
    private String tableInfoSql;
    private String tableFieldSql;
    private String tableName;
    private String tableComment;
    private String columnName;
    private String columnType;
    private String columnKey;
    private String columnGenerator;
    private Map<Pattern, String> convertMap;

    public String getJavaType(String dbType) {
        String javaType = Constant.TYPE_STRING;
        String finalDbType = dbType.toLowerCase().trim();
        for (Map.Entry<Pattern, String> entry : convertMap.entrySet()) {
            if (entry.getKey().matcher(finalDbType).find()) {
                javaType = entry.getValue();
                break;
            }
        }
        return javaType;
    }
}



