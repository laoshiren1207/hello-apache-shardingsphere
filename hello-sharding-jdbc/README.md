## Sharding-JDBC
本项目采用`mysql`+`mybatis-Plus`开发，创建2个数据库。
#### mysql 环境
~~~sql 
-- 数据库0 
CREATE DATABASE `db_order_0` 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;

-- 数据库1
CREATE DATABASE `db_order_1` 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
~~~
在每一个数据库创建2张表
~~~sql 
-- 数据库0
use db_order_0;

CREATE TABLE `tb_order_0` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tb_order_1` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 数据库1
use db_order_1;

CREATE TABLE `tb_order_0` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tb_order_1` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
~~~
还得创建一个模型表，因为使用mybatis-Plus生成代码，所以在0号数据库生成一个逻辑表，表结构和分表的表结构一致
~~~sql 
CREATE TABLE `tb_order` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
~~~

#### 添加依赖
主要是`mysql`的依赖版本问题需要注意
~~~xml 
<!-- MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <!-- MySQL 驱动的版本号必须是 5.1.48 -->
    <version>5.1.48</version>
</dependency>
<!-- MyBatis-Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.2.0</version>
</dependency>
<!-- Apache ShardingSphere -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.0.0-RC3</version>
</dependency>
~~~

#### 配置文件
~~~yaml 
spring:
  main:
    allow-bean-definition-overriding: true
  shardingsphere:
    props:
      sql:
      # 是否显示sql语句 会将逻辑语句和实际执行的语句打印到控制台
        show: true
    datasource:
      # 分库分表 可以配置多个数据源
      names: ds0,ds1
      # 数据库0 
      ds0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://192.168.8.50:3306/db_order_0?useUnicode=true&characterEncoding=utf-8&serverTimezone=Hongkong&useSSL=false
        username: root
        password: root
        hikari:
          minimum-idle: 5
          idle-timeout: 600000
          maximum-pool-size: 10
          auto-commit: true
          pool-name: MyHikariCP
          max-lifetime: 1800000
          connection-timeout: 30000
          connection-test-query: SELECT 1
    # 分库规则
    sharding:
      # 逻辑表 可以多个 ,分隔
      binding-tables: tb_order
      default-database-strategy:
        inline:
          # user_id 对2取余，几个数据库就对多少取模，这样就可以选择 ds0,ds1...ds9
          algorithm-expression: ds$->{user_id % 2}
          sharding-column: user_id
      tables:
        tb_order:
          actual-data-nodes: ds$->{0..1}.tb_order_$->{0..1}
          table-strategy:
            inline:
              # 使用order_id 分别打在不同表上 
              algorithm-expression: tb_order_$->{order_id % 2}
              sharding-column: order_id
~~~

#### 执行sql
##### 查询全部
~~~java 
QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
List<TbOrder> tbOrders = mapper.selectList(queryWrapper);
~~~
日志，实际查找了4张表 
~~~shell 
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Rule Type: sharding
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Logic SQL: SELECT  id,order_id,user_id  FROM tb_order
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : SQLStatement: SelectSQLStatementContext(super=CommonSQLStatementContext(sqlStatement=org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement@509a6095, tablesContext=TablesContext(tables=[Table(name=tb_order, alias=Optional.absent())], schema=Optional.absent())), projectionsContext=ProjectionsContext(startIndex=8, stopIndex=26, distinctRow=false, projections=[ColumnProjection(owner=null, name=id, alias=Optional.absent()), ColumnProjection(owner=null, name=order_id, alias=Optional.absent()), ColumnProjection(owner=null, name=user_id, alias=Optional.absent())]), groupByContext=org.apache.shardingsphere.core.preprocessor.segment.select.groupby.GroupByContext@57cabdc3, orderByContext=org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByContext@75bd28d, paginationContext=org.apache.shardingsphere.core.preprocessor.segment.select.pagination.PaginationContext@129c4d19, containsSubquery=false)
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_0
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_1
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds1 ::: SELECT  id,order_id,user_id  FROM tb_order_0
2020-07-01 12:51:07.113  INFO 21232 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds1 ::: SELECT  id,order_id,user_id  FROM tb_order_1
~~~

##### 插入数据
~~~java 
for (int i = 0; i < 100 ; i++) {
    TbOrder tbOrder = new TbOrder();
    tbOrder.setUserId(Long.parseLong(""+i));
    // 随机打在某个库的2张中的1张表
    tbOrder.setOrderId(new Double((Math.random()*2)).longValue());
    mapper.insert(tbOrder);
}
~~~
日志，库是根据`userId`对2取余得到的结果，表是`orderId`随机生成后对2取余的结果()
~~~shell
2020-07-01 12:55:01.704  INFO 17012 --- [           main] ShardingSphere-SQL                       : Rule Type: sharding
2020-07-01 12:55:01.704  INFO 17012 --- [           main] ShardingSphere-SQL                       : Logic SQL: INSERT INTO tb_order  ( order_id,user_id )  VALUES  ( ?,? )
2020-07-01 12:55:01.704  INFO 17012 --- [           main] ShardingSphere-SQL                       : SQLStatement: InsertSQLStatementContext(super=CommonSQLStatementContext(sqlStatement=org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement@61b838f2, tablesContext=TablesContext(tables=[Table(name=tb_order, alias=Optional.absent())], schema=Optional.absent())), columnNames=[order_id, user_id], insertValueContexts=[InsertValueContext(parametersCount=2, valueExpressions=[ParameterMarkerExpressionSegment(startIndex=55, stopIndex=55, parameterMarkerIndex=0), ParameterMarkerExpressionSegment(startIndex=58, stopIndex=58, parameterMarkerIndex=1)], parameters=[1, 2])])
2020-07-01 12:55:01.705  INFO 17012 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: INSERT INTO tb_order_1  ( order_id,user_id )  VALUES  (?, ?) ::: [1, 2]
~~~
##### 查询单条记录
分2种情况
1. 如果是走了`sharding-jdbc`的`sharding-column`键，那么实际查询查其中分库分表的一部分
~~~java 
QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("user_id",32);
List<TbOrder> tbOrders  = mapper.selectList(queryWrapper);
~~~
日志
~~~shell
2020-07-01 13:01:20.389  INFO 20300 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_0 WHERE (user_id = ?) ::: [32]
2020-07-01 13:01:20.389  INFO 20300 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_1 WHERE (user_id = ?) ::: [32]
~~~

2. 如果没走`sharding-column`键，就会全库全表查询。
~~~java 
QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("id",220);
List<TbOrder> tbOrders  = mapper.selectList(queryWrapper);
~~~
日志
~~~shell 
2020-07-01 13:04:13.088  INFO 13268 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_0 WHERE (id = ?) ::: [220]
2020-07-01 13:04:13.088  INFO 13268 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds0 ::: SELECT  id,order_id,user_id  FROM tb_order_1 WHERE (id = ?) ::: [220]
2020-07-01 13:04:13.088  INFO 13268 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds1 ::: SELECT  id,order_id,user_id  FROM tb_order_0 WHERE (id = ?) ::: [220]
2020-07-01 13:04:13.088  INFO 13268 --- [           main] ShardingSphere-SQL                       : Actual SQL: ds1 ::: SELECT  id,order_id,user_id  FROM tb_order_1 WHERE (id = ?) ::: [220]
~~~

回头再看这张图，就可以明白实现的基本操作,就可以理解`sharding-jdbc`如何分库分表的操作了
<img src="https://shardingsphere.apache.org/document/legacy/4.x/document/img/sharding-jdbc-brief.png" style="zoom:65%;" />
