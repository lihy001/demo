# 


# 

 1、将pom.xml文件中的<lucene.version>7.5.0</lucene.version>改成对应的版本号

 2、执行mvn install  

 3、需要使用分词器的项目添加依赖

```pom
	<dependency>
		<groupId>com.heng</groupId>
		<artifactId>ik-analyzer</artifactId>
		<version>7.5.0</version>
	</dependency>
```
version改成对应的版本号

