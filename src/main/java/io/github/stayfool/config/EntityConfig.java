package io.github.stayfool.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * @author stay fool
 * @date 2017/8/19
 */
@Data
public class EntityConfig {

    @Parameter
    private String pkg = "entity";

    @Parameter
    private Boolean useLombok = false;

    @Parameter
    private String template = "entity.vm";

    @Parameter
    private String superClass;

    @Parameter
    private List<String> excludeFields;

    @Parameter
    private Boolean overrideSuperClassField = false;

    @Parameter
    private Boolean needColumnAnnotation = true;

    @Parameter
    private String idType;
}
