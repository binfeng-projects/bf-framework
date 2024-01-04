
-------------------------------------------------------------------------------

## 项目初衷和目标

### 一：让微服务开发更快一点，让业务开发更简单一点。
本框架基于spring和spring-boot封装。可作为通用中间件接入框架，减少业务开发的门槛。众所周知:spring
已经成为了Java业务开发事实上的工业标准,spring-boot更是将业务开发的复杂度降低到了一个前所有未有的程度。
那么是不是每个公司每个业务只要用上spring就能无往而不利了呢？从事Java开发多年的朋友肯定都明白，显然不是的，
业务要远比我们想象 的复杂和个性化。而业务开发也不可避免的淹没在了各种中间件和技术细节里。下面举例几个场景说明：
1. spring-boot很多自动装配都是基于单实例的，也就是默认你一个服务只连一个实例的中间件。比如你去网上搜一下，spring-boot接入多个数据源，那业务的代码量
   和复杂度可能一下子就上去了，我们应该给业务选择的权利。并保障接入体验是一致的。实际业务中，一个服务需要连多个db实例,连多个elasticsearch集群的例子比比皆是。
2. spring并不清楚你们的公司的业务特点，和使用的技术栈以及中间件。spring原生作为顶级项目支持的中间件(例如kafka,pulsar)并不多。很多国内优秀的中间件，
   像阿里系的dubbo啦，rocketmq啦。中小企业很多都在用的xxl-job啦，配置中心Apollo啦，这些如果想要方便的丝滑的接入业务，还是得中间件团队包掉。
3. spring并不清楚你们公司的业务是否异地多集群，有没有海外业务，这些业务会不会导致你开发的时候还能像原生一样舒服?比如，spring-boot可以基于profile
   配置环境，一般分几个环境dev,test,pre,prod。但是一旦多了集群的概念，甚至区域加集群，那么复杂度又会上去。这些同样需要中间件团队来解决。
4. 再来看，业务开发同学的诉求，他们的诉求是什么？那就是除了实现业务我什么都不想关心，这就是他们最朴素的诉求。有一个Java界大牛的故事非常能说明这一点：
   世界上流行甚广的ORM框架，hibernate，其实是一个一开始非常不擅长也很讨厌写SQL的人（Gavin King）写的。他当初抱怨，SQL影响了他对业务的关注。
   他只想实现业务，不想关心SQL。不想知道那么多的技术细节。
5. 一言以蔽之，spring作为通用型框架，是不知道你的很多业务细节的，也不应该去知道。spring给我们搭建好了一个稳固和极具扩展性的地基。而我们则需要在地基上搭积木。
   这个积木理想情况下，应该是业务的积木，而不是其他非业务相关的（包括技术）。但是很遗憾，很多时候，业务工程师不得不关注到很多其实本身和业务不大有关系的细节。


### 二：做一个方便的工具框架。
经过封装以后，中间件接入起来真的非常方便。本身就简化了很多中间件的接入，更有代码生成工具，常用的一些工具包等的封装，详见项目feature

### 三：希望可以给到一些初学者一些帮助，也希望能有机会为开源贡献一份自己的力量
1. 曾几何时,想起当初职业生涯刚起步的时候，一张Java技术栈学习路线图，伴随着我渡过了不少个春秋。现在回过头来看，当初都是靠一些没法上生产的demo例子
代码入门，然后一点点在项目中，用到啥啃啥，不成体系。如果有一个整合的中间件框架可以伴随路线图一起学习，是不是会走一些弯路。
2. 我们 几乎每个技术人都在享受着开源带来的福利，也是本着开源的精神,希望自己也有机会能回馈开源，为开源贡献一份自己的力量。除了本项目，所有本人维护
的项目，均会去抽象一些通用的业务模型,从实际生产应用落地出发（会更接地气一点）。造一些可以复用的业务轮子， 或者说小一点，可以让自己和用到的人
有一个可以抄代码的地方。比如通用权限，通用支付模块，通用三方登录等等。哪怕有一个人用了，或者看了我的代码而节省了哪怕一点点的时间，也是做出我的贡献，
是吧？再不行，就当给自己一个沉淀总结，给自己造一个可以复用的代码库吧。
3. 都是在工作之外业余时间弄的，也有家室有孩子，输出时间很难得到保障。所以靠的是日积月累，细水长流。如果有问题需要联系，可以先加QQ(wechat就不交换啦)，
搜索QID：luckybf008 验证信息说明:来自git。

-------------------------------------------------------------------------------

## 项目feature

### 支持的中间件
1. 支持涵盖了中小企业开发大型高并发分布式业务的常用的一些技术栈和中间件。例如db,redis,elasicsearch,kafka,dubbo,zk,
2. 有对大数据hadoop技术栈的完整支持：支持hdfs,hbase,hive,yarn,flink。底层文件存储既支持原生hdfs,也支持各大云厂商对象存储系统
3. 有对云服务厂商的支持，例如对象存储服务（目前支持AWS s3，阿里云OSS，腾讯云COS），语音服务（阿里云和腾讯云VMS）。
4. 配合Prometheus和grafana,支持应用监控服务。支持链路追踪中间件skywalking
5. 支持任务job框架xxl-job，支持spring轻量级批处理框架spring-batch
6. TO DO more...后续还会加入更多流行实用的中间件，比如消息中间件，目前支持kafka, 后续会考虑支持rocketmq和pulsar。
保持readme的干净。后续迭代更新方面的细节就放到change-log.txt中说明,主页说明些脉络性的整体feature

### 极其简单的多实例配置
大多数中间件（除了极个别哈，比如xxl-job。我实在想不出你有啥场景需要支持接入多个实例），
你只需要配置四个选项就能用上这个中间件。url,用户名，密码，再加一个是否启用标识。多个实例，只需要重复这四个配置。
很多调优配置有默认值自动继承，当然也支持业务覆盖(配合Apollo等配置中心，可以实现非常灵活，多优先级的覆盖规则,业务代码不再写死，
配置复杂度又如同单实例。）支持集群维度的扩展。 一个示例配置如下:
```yaml
bf: #配置了三个数据源，启用其中两个
    datasource:
        enabled: 'order-center,pay-center'
        order-center:
            url: jdbc:mysql://127.0.0.1:3306/order_center?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true
            username: root
            password: 111111
        pay-center:
            url: jdbc:mysql://127.0.0.1:3306/pay_center?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true
            username: root
            password: 111111
        test-db:
            url: jdbc:mysql://127.0.0.1:3306/test-db?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true
            username: root
            password: 111111
```
甚至可以进一步简化。最佳实践是，业务不要直接对接bf-framework。中间件团队再抽一个工程来（可参考[middleware-integration](https://github.com/binfeng-projects/middleware-integration)）对接bf-framework。
middleware-integration可配置全公司常用的所有中间件。其他业务项目都依赖middleware-integration即可。那么业务最极致简单的情况下，只需要一行配置，如下：

```yaml
bf: #配置了三个数据源，启用其中两个
    datasource:
        enabled: 'order-center,pay-center'
```
业务甚至都不关心地址，账户密码。我仅仅只是想要启我想要启用的实例。至于它的细节我不关心。

### 对大数据hadoop生态的支持
1. 本项目对大数据技术栈的支持基于[spring-hadoop](https://github.com/binfeng-projects/spring-hadoop)项目,spring官方已经放弃了对该项目
的维护，官方一些依赖都已经无法down到本地（不是公开的maven仓库）。本人fork该项目的源码，并迭代更新，使hadoop技术栈和spring结合一起。
所以该模块依赖需要[spring-hadoop](https://github.com/binfeng-projects/spring-hadoop)打出依赖包。关于该项目细节，具体也可移步主页了解。
2. 实际工作中，发现这样一个现象，常规业务开发和大数据通常是两个部门。常规业务开发的同学是不了解大数据技术栈的。
而了解大数据技术栈的往往对SQL比较熟，工程规范和开发这块可能欠缺点,甚至都不了解spring。个人认为作为一个javaer或者大数据er,还是需要完整的吧
3. 关于spring-hadoop。有两种使用方式。你依然可以clone并打出包来，按照网上还能搜到的spring-hadoop官方教程来使用。但不建议了。
建议并欢迎使用bf-framework接入hadoop。

### 关于运行环境
1. 本项目最小依赖spring-boot3.x版本。所以需要JDK17以上。
2. 默认依赖本人自己打包出的[spring-boot](https://github.com/binfeng-projects/spring-boot)（切换成任意官方3.x以上都是兼容的）
3. 另外所有本人维护的项目maven都会继承[bf-pom](https://github.com/binfeng-projects/bf-pom)打出来的parent父pom
4. 这里说明下，国内可能主流还是JDK1.8。个人建议该升级了，当前（截止2024-01），JDK最新release都已经到21了。无论是spring还是kafka,还是其他一众大型中间件，都已经
   明确开始放弃1.8了。所以，it is time! 咱还是升级吧，不能落后了。

### 对flink的支持
因为spring-boot3最小需要JDK17。而截止当前（2024-01），flink最新release版本（1.18）仍未支持，但是最新的master分支源码已经支持，
所以在1.19发版的时候，就会支持。当前本工程是用[flink源码](https://github.com/binfeng-projects/flink)master分支打出来的依赖
（后续等官方release了，直接改个版本号就行了），亲测兼容JDK17

### 项目工程结构（可对照主页代码根目录看）
项目工程严格按照spring-boot工程规范划分模块。并大量借鉴了spring-boot源码。
1. bf-framework-common是整个项目最底层的依赖，主要包装一堆的工具util类。该模块主要依赖一个时下非常流行的国产工具类库[hutool](https://hutool.cn/)
这里顺便也是推荐一下该项目，确实挺方便的，想想你项目中一般会依赖哪些常用的工具类库？apache-common那一堆少不了吧？guava可是你的心头爱？还有一堆的网络类库，
excel处理啦，加解密啦什么的。试试看这个hutool这个吧。除了比较全面外，个人更喜欢他对依赖的严格控制，不会引入额外的过多依赖，干净清爽。bf-framework-common
也可以作为你项目的一些通用基础工具类模块，对hutool又包了一层，方便后续换类库。
2. bf-framework-boot模块依赖spring-boot和bf-framework-common。是一些和具体中间件无关，只和spring有关的基础模块。里面有一些对spring的包装和一些通用的抽象支持。
3. bf-framework-starters模块和bf-framework-autoconfigure模块，熟悉spring-boot的朋友肯定就很熟悉了，依赖bf-framework-boot和各自需要支持的中间件类库
4. bf-framework-test依赖spring-boot-starter-test,可以作为业务开发项目的测试依赖。该模块还支持自动代码生成等一系列实用的工具，让你在run单元测试的时候，生成出
完美匹配本框架的代码。像一般代码生成工具有的泛型CRUD，自然是不在话下。该模块甚至把通用CRUD扩展到了Elasticsearch等其他中间件。具体使用需要后续出一些文档或者example工程，敬请期待。
不是初学者的可先直接看源码。

-------------------------------------------------------------------------------
## 📝文档 (待详细补充)
抱歉，可能暂时就没时间详细出了。个人建议 不是初学者的直接看源码吧（其实初学者也是建议直接看源码），程序员之间，或者程序员和开源项目之间交流的最好方式就是看源码。
上面提到的接入bf-framework最佳实践的[middleware-integration](https://github.com/binfeng-projects/middleware-integration)工程，该项目中的单元
测试代码可以作为example工程，看源码和example永远是了解项目最好最快的方式。顺便提一下，需要本地看spring源码又主要构建工具是maven的 可以移步本人主页。为了方便看源码和随
时能自己本地源码打包，本人已经将spring和spring-boot工程maven化改造，欢迎clone和star

------------------------------------------------------------------------------

## ⭐Star & donate
爆肝不易。开源不易。如果本人的任何项目，任何文档，任何话术恰巧有帮助到你一点点的话，也可以考虑赞一杯星爸爸。
- [![zsm.jpg](https://cdn.jsdelivr.net/gh/luckybf/resource@main/pic/zsm.jpg)](https://smms.app/image/uy2lF5CLjpUsK83)
- [![zfb.png](https://cdn.jsdelivr.net/gh/luckybf/resource@main/pic/zfb.png)](https://smms.app/image/mE1LTl8UAeIGWJX)

------------------------------------------------------------------------------
## 如果上面打赏图片无法显示，可打开下面二维码
### <a target="_blank" href="https://g-dmwl1346.coding.net/public/java/bf-pom/git/files/main/zsm.jpg">wechatpay</a>
### <a target="_blank" href="https://g-dmwl1346.coding.net/public/java/bf-pom/git/files/main/zfb.png">alipay</a>
