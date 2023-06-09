个人常用的spring boot应用配置代码

注意：
1.如果是SpringGateway网关的应用引入本包，则需要排除spring-boot-starter-web包的引入
```
<dependency>
			<groupId>com.github.qqxadyy</groupId>
			<artifactId>pjq-spring-boot-starter</artifactId>
			<exclusions>
				<exclusion>
					<!-- gateway模块基于webflux的，引入starter-web会启动报错 -->
					<!-- ServerCodecConfigurer is that could not be found -->
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-web</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
```
