package io.github.stayfool.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author stay fool
 * @date 2017/8/15
 */
@Data
public class DatasourceConfig {

    @Parameter(required = true)
    private String driverClass;

    @Parameter(required = true)
    private String url;

    @Parameter(required = true)
    private String username;

    @Parameter(required = true)
    private String password;
}
