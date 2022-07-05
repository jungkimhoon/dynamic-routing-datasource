# dynamic-routing-datasource

Multiple DB Connection이 필요한 경우 각 DB마다 Mapper를 지정해서 세팅할수도 있지만,

DB가 추가되거나 삭제되는 경우 그에 의존되는 파일이나 설정을 추가적으로 관리해주어야 하기 때문에

yml 파일에서 동적으로 설정 값을 읽어 필요한 db를 사용하도록 하는 목적


## application.yml
- yml에서 spring.db1, db2 ... 형식으로 동적으로 활용할 db 정보를 입력받음
``` console
spring:
    db1:
        datasource:
            driver-class-name: org.postgresql.Driver
            jdbc-url: jdbc:postgresql://127.0.0.1:11300/test_db1
            username: test1
            password: 1234
    db2:
        datasource:
            driver-class-name: org.postgresql.Driver
            jdbc-url: jdbc:postgresql://127.0.0.1:11300/test_db2
            username: test2
            password: 1234
    db3:
        datasource:
            driver-class-name: org.postgresql.Driver
            jdbc-url: jdbc:postgresql://127.0.0.1:11300/test_db3
            username: test3
            password: 1234

                .
                .    
                .
```

## MyBatisConfig.java
[MyBatisConfig.java](https://github.com/jungkimhoon/dynamic-routing-datasource/blob/main/src/main/java/com/example/dynamicroutingdatasource/config/MyBatisConfig.java)

- yml 파일에서 설정한 db 값들을 읽어 AbstractRoutingDataSource 인터페이스에 등록하는 역할.

## RoutingDataSource.java
- MyBatisConfig에 설정된 routingDataSource는 determineCurrentLookupKey 메서드에 의해 호출되도록 함
``` console
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return RoutingDatabaseContextHolder.getClientDatabase();
    }
}
```

## RoutingDatabaseContextHolder.java
- ThreadLocal을 통해 각 쓰레드가 참조해야할 DB Connection을 관리 할 수 있다.
``` console
public class RoutingDatabaseContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String db) {
        CONTEXT.set(db);
    }

    public static String getClientDatabase() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
```

## Use RoutingDataSource
- 실제 사용
- RoutingDatabaseContextHolder에 각 db key값을 setting하여 routingDataSource를 사용할 수 있다.
``` console
RoutingDatabaseContextHolder.set(String.format("db%s", dbSeq)); // set RoutingDatabaseContextHolder
ret = arjsMapper.getMapDataApi(payload);                        // get Data by Selected DB
RoutingDatabaseContextHolder.clear();                           // clear RoutingDababaseContextHolder
```
