# 基于dubbo的分布式系统调用跟踪demo

Quick Start：

+ 确保[zipkin server](http://zipkin.io/)已经正常启动；
+ 确保[zookeeper](http://zookeeper.apache.org/)已经正常启动：
+ 分别运行以下命令：

	+ 克隆项目：
	
		```bash
		git clone git@github.com:ihaolin/dubbo-trace.git
		```
	
	+ 本地安装：
	
	    ```bash
	    mvn clean install -DskipTests
	    ```
	
	+ 编辑``trace-demo``各项目对应配置：

		+ app.properties：

			```
			# dubbo注册中心
			dubbo.registry=zookeeper://localhost:2181
			```
		
		+ trace.yml:

			```
			# ...

			# zipkin-server http地址
			server: 'localhost:9411'

			# ...
			```
		
	+ 安装项目：

		```bash
		mvn clean install -DskipTests
		```
	
	+ 运行user服务:

		```bash
		./run_user.sh
		```
	
	+ 运行order服务:

		```bash
		./run_order.sh
		```
	
	+ 运行web服务：

		```bash
		./run_web.sh
		```
	
	+ 在浏览器请求：

		```
		http://localhost:10000/api/orders/create
		```
	
	+ 即可zipkin-server中查看跟踪记录。

+ 具体细节可参见[这篇文章](https://t.hao0.me/devops/2016/10/15/distributed-invoke-trace.html)。