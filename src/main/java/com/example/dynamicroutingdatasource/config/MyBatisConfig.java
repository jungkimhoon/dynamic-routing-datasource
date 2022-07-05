package com.example.dynamicroutingdatasource.config;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestContextListener;

import java.util.*;

/**
 * @see MyBatis 설정 클래스
 *
 * Dynamically set datasources from yml
 */
@Configuration
@RequiredArgsConstructor
@MapperScan("com.example.dynamicroutingdatasource.mapper")
public class MyBatisConfig {

    private final Environment env;

    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }

    @Bean(name = "dbDatasource")
    public DataSource RouterDatasource() {
        AbstractRoutingDataSource routingDataSource = new MyRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        List<Map> list = this.getDbList();

//        System.out.println(list);
        for(Map map : list) {
            String db = map.get("db").toString();
            String jdbcUrl = map.get("jdbc-url").toString();
            String userName = map.get("username").toString();
            String password = map.get("password").toString();

            DataSource datasource = createDataSource(jdbcUrl, userName, password);
            targetDataSources.put(db, datasource);
        }

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(targetDataSources.get("db1"));

        return routingDataSource;
    }

    @Bean(destroyMethod = "clearCache")
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    private DataSource createDataSource(String url, String user, String password) {
        com.zaxxer.hikari.HikariDataSource dataSource = new com.zaxxer.hikari.HikariDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);

        return dataSource;
    }

    public List<Map> getDbList() {
        Map<String, String> properties = new LinkedHashMap<>();

        // Default profile
        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                        if (key.startsWith("spring.db")) {
                            properties.put(key, propertySource.getProperty(key).toString());
                        }
                    }
                }
            }
        }

        // Active profile
        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    if(env.getActiveProfiles().length > 0 && propertySource.getName().contains(env.getActiveProfiles()[0])) {
                        for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                            if (key.startsWith("spring.db")) {
                                properties.put(key, propertySource.getProperty(key).toString());
                            }
                        }
                    }
                }
            }
        }

        return this.parseList(properties);
    }

    private List<Map> parseList(Map<String, String> map) {
        List<Map> list = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();

        for (String key : map.keySet()) {
            String db = key.split("[.]")[1];
            set.add(db);
        }

        for (String db : set) {
            Map<String, String> dbMap = new HashMap<>();
            dbMap.put("db", db);

            for (String key : map.keySet()) {
                if (key.contains(db)) {
                    dbMap.put(key.split("[.]")[3], map.get(key));
                }
            }

            list.add(dbMap);
        }

        return list;
    }
}
