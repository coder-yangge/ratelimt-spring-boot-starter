# 使用教程

### 1.使用场景

在一些特殊的场景下，需要对一些方法或接口进行限流、限制，比如在用户注册或者需要验证码校验的接口，通常需要发送短信验证码，一分钟只能发送一次，一天限制发送10次等（为防止恶意攻击），或者在对某些方法或接口的调用频率需要限制时。<u>如果使用者的系统本身很庞大，则可以使用alibaba的Sentinel: 分布式系统的流量防卫兵，来进行控制。</u>（本组件为轻量级）

### 2.使用方式

需要依赖redis，自行配置yml种redis配置

拉取代码到本地，执行mvn install，发布到本地仓库

maven pom引入

```xml
<dependency>
    <groupId>com.ratelimiter</groupId>
    <artifactId>ratelimit-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

启动类上添加@EnableLimit

在需要限制的方法或者接口上添加@Limit注解， 其中属性key与userId为spel表达式。

spel: https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html

案例

1. 短信验证码限制，每个手机号一分钟一次，1天10次

   ```java
   @Limit(ruleGroup = {
           @Rule(name = "minuteLimit", limit = 1, expire = 1, unit = TimeUnit.MINUTES, exceptionMsg = "1秒中限制1次"),
           @Rule(name = "DayLimit", limit = 10, expire = 1, unit = TimeUnit.DAYS, exceptionMsg = "1天限制10次", order = 1)})
   public void sendMsg(@Key String phoneNo, String msg) {
       // 发送短信代码
   }
   ```

   或者如下，其中key与userId为spel表达式，可通过方法入参进行获取

   ```
   @Limit(key = {"#phoneNo"}, ruleGroup = {
           @Rule(name = "minuteLimit", limit = 1, expire = 1, unit = TimeUnit.MINUTES, exceptionMsg = "1秒中限制1次"),
           @Rule(name = "DayLimit", limit = 10, expire = 1, unit = TimeUnit.DAYS, exceptionMsg = "1天限制10次", order = 1)})
   public void sendMsg(String phoneNo, String msg) {
       // 发送短信代码
   }
   ```

   

2. 普通的限制： 1分钟限制100次，其中key为test，详情可查看spel表达式

   ```java
   @Limit(key = {"'test'"}, ruleGroup = {
           @Rule(name = "secondLimit", limit = 100, expire = 1, exceptionMsg = "1分钟中限制100次")})
   @GetMapping("/limit/test")
   public ResponseEntity<String> test() {
   
       return ResponseEntity.status(200).body("OK");
   }
   ```

3. 带用户的限制： 每个用户每天限制10次

   ```java
   @Limit(key = {"'dosomething'"}, ruleGroup = {
           @Rule(name = "DayLimit", limit = 10, expire = 1, unit = TimeUnit.DAYS, exceptionMsg = "1天限制10次", order = 1)})
   public void doSomeThing(@User String user) {
       // 具体业务逻辑
   }
   ```

   或者

   ```
   @Limit(key = "'dosomething'", userId = "#user", ruleGroup = {
           @Rule(name = "DayLimit", limit = 10, expire = 1, unit = TimeUnit.DAYS, exceptionMsg = "1天限制10次", order = 1)})
   public void doSomeThing(String user) {
       // 具体业务逻辑
   }
   ```

   若参数为java对象如，具体查看spel表达式

   ```
   @Limit(key = {"#company.id"}, ruleGroup = {
           @Rule(name = "secondLimit", limit = 1, expire = 2, exceptionMsg = "2秒中限制1次")})
   @GetMapping("/limit/test")
   public ResponseEntity<String> test(Company company) {
   
       return ResponseEntity.status(200).body("OK");
   }
   ```

### 3.@Limit注解介绍

接口限制原理为采用redis储存接口或者方法调用的次数，由于redis处理客户端请求时为单线程执行，所以利用lua脚本来进行key的设置、过期时间设置，调用次数计数。

在redis中

​		**key的组成为： prefix(可为空) +userId (可为空)+ key(可为空) + @Rule中的name(可为空)**

@Key的优先级别最高，在@Key为空的情况下，才会取Limit中的key，Limit中的key为空时，取值类名+方法名

当方法参数中有@Key修饰的参数时，@Key的优先级别最高，如上述例子。

当方法入参有@User修饰的参数时，userId取该参数；若没有@User修饰的参数时，取Limit中的userId；

```java
public @interface Limit {
	
    /**
      * redis中key的前缀
      */
    String prefix() default "";

    /**
     * key为spel表达式 如#{bean.id}，作为redis中的key, 如果key为空，则将类名+方法名作为key
     */
    String key() default "";

    // 规则组，用来定义限制规则
    Rule[] ruleGroup();

    // redis key在进行拼接时的分隔符
    String separator() default ":";

    /**
     * 用户 userId为spel表达式
     */
    String userId() default "";
}
```

```java
public @interface Rule {

    // 规则名称，如果name不为空，redis中的key会拼接该名称
    String name();
    
	// 限制次数 默认10
    long limit() default 10L;

    // 过期时间 默认1分钟
    long expire() default 1L;
	
    // 过期时间单位，默认分钟
    TimeUnit unit() default TimeUnit.SECONDS;

    // 规则次序，在多个规则的情况下，会根据order排序
    int order() default 5;
	
    // 在限制后抛出的异常信息
    String exceptionMsg() default "";
}
```