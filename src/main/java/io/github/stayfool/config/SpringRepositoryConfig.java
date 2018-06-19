package io.github.stayfool.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author stay fool
 * @date 2017/8/19
 */
@Data
public class SpringRepositoryConfig {

    @Parameter
    private String pkg = "repository";

    @Parameter
    private String template = "repository.vm";

    @Parameter
    private String superInterface = "org.springframework.data.jpa.repository.JpaRepository";
}
