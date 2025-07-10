package org.chemtrovina.cmtmsys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.chemtrovina.cmtmsys")
public class SpringContextConfig {
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSourceConfig.getDataSource());
    }
}
