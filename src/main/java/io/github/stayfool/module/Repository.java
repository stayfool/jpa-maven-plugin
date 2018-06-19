package io.github.stayfool.module;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author paoding
 * @date 2017/12/6
 */
@Builder
@Data
public class Repository {

    private String pkg;
    private String author;
    private String date;
    private String table;
    private String name;
    private String superInterface;
    private String entityName;
    private String entityIdType;

    private List<String> imports;
}
