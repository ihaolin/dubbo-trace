## 1.原理
从url开始进行trace.因此，需要配置url.
如果url不进行配置，是跟踪不了的。

> 注:1.1.0版本，增加了直接问我consumer拦截功能，不需要从url进行拦截了.

## 2.配置方法
顺序为：
- yaml配置
- url配置
- consumer配置
- provider配置
以上几个个步骤，缺一不可。

### 2.0 yml配置
在classpath下的resouces目录下面。
创建文件track.yml。
其中，url类型和非url类型是不同的。
- url类型，需要配置points.
- 非url类型，不需要配置points.

#### 2.0.1 url类型配置

```

# Enable trace or not
enable: true

# The zipkin trace server
server: 'localhost:9411'

# The app name
name: 'test'

# The app owner
owner: 'njkfei'

# The trace points
points:

  - key: user
    pattern: ^/user/.*
    desc: user_info
  - key: order
    pattern: ^/order/.*
    desc: order_info

```

#### 2.0.2 非url类型配置
```
# Enable trace or not
enable: true

# The zipkin trace server
server: 'localhost:9411'

# The app name
name: 'springbootprovideruser'

# The app owner
owner: 'njkfei'
```

### 2.1 url配置
#### 2.1.1 配置url
```
# The trace points
points:

  - key: user
    pattern: ^/user/.*
    desc: user_info
  - key: order
    pattern: ^/order/.*
    desc: order_info
```

#### 2.1.2 配置filter拦截器
使用java config.代码如下：
```
@Configuration
@EnableAutoConfiguration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(traceFilter());
        registration.addUrlPatterns("/user/*");  // * 号不能丢，否则无法拦截
        registration.addUrlPatterns("/order/*");  // * 号不能丢，否则无法拦截
        registration.setName("traceFilter");
        return registration;
    }

    @Bean(name = "traceFilter")
    public Filter traceFilter() {
        return new TraceFilter();
    }
}
```

### 2.2 consumer注解配置

> 消费服务时，需要指定filter类型为"TraceConsumerFilter"

```
    @Reference(version="1.0.0",filter="TraceConsumerFilter")
    public  com.jinhui.api.service.UserService userService;

    @Reference(version="1.0.0",filter="TraceConsumerFilter")
    public com.jinhui.api.service.OrderService orderService;
```

### 2.3 provider注解配置

> 提供服务时，需要指定filter类型为“TraceProviderFilter”

```
@Service(version="1.0.0",filter="TraceProviderFilter")
public class UserServiceImpl implements UserService {
}
```
