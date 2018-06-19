package io.github.stayfool.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author stay fool
 * @date 2017/8/17
 */
@Data
public class GenerateConfig {

    @Parameter
    private String baseDir;

    @Parameter
    private String basePkg;

    @Parameter
    private Boolean override = true;

    @Parameter
    private EntityConfig entity = new EntityConfig();

    @Parameter
    private SpringRepositoryConfig repository;
}
