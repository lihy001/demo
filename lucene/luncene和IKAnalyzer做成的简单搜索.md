### luncene和IKAnalyzer做成的简单搜索

#####一、做成后的效果图



![](search1.png)

##### 二、代码说明

###### 1、基本配置

application.properties中

```properties
luncene.data-path=F://lunceneDataPath
luncene.index-path=F://lunceneIndexPath
```

在luncene.data-path目录下准备好需要索引的文件，

暂时只能对txt文件进行索引，如果需要添加对其他文件的额外支持。需要在IndexServiceImpl.createIndex

```			java
			if ("txt".equalsIgnoreCase(type)) {
				content += txt2String(file);
			}
```

后面添加额外的代码。



IK分词器说明，ik-analyzer中的版本需要和luncene版本一致，建议修改该项目同级目录下的IKAnalyzer工程

修改其pom文件中的luncene的版本，然后将IKAnalyzer依赖添加到需要的工程上。

