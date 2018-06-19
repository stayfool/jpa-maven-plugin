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
public class Entity {

    private String pkg;
    private String comment;
    private String author;
    private String date;
    private String table;
    private String name;
    private String superClass;
    private boolean useLombok;
    private boolean needColumnAnnotation;

    private List<String> imports;
    private List<EntityField> fields;
}
