package io.github.stayfool.module;

import lombok.Data;

/**
 *
 * @author stay fool
 * @date 2017/8/15
 */
@Data
public class EntityField {
    private boolean id;
    private String name;
    private String type;
    private String column;
    private String generator;
    private String comment;
    private String method;
}
