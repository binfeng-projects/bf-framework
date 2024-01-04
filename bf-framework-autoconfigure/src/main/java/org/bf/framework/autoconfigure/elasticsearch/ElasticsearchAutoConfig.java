package org.bf.framework.autoconfigure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.SimpleJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnClass({RestClientBuilder.class,ElasticsearchTemplate.class})
@ConditionalOnMissingBean(value = ElasticsearchAutoConfig.class)
@EnableConfig(ElasticsearchAutoConfig.class)
@Slf4j
public class ElasticsearchAutoConfig implements EnableConfigHandler<ElasticsearchProperties> {
    private static final String PREFIX = PREFIX_ELASTICSEARCH;
    @Override
    public String getPrefix() {
        return PREFIX;
    }
    @Override
    public ElasticsearchProperties bindInstance(Map<String, Object> properties) {
        return createConfig(properties);
    }
    public static ElasticsearchProperties createConfig(Map<String, Object> properties) {
        return new ElasticsearchProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> cfgMap, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(cfgMap);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            ElasticsearchProperties cfg = (ElasticsearchProperties)YamlUtil.getConfigBind(cfgMap);
            RestClientTransport transport = new RestClientTransport(createRestClient(cfg), new SimpleJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);

            ElasticsearchCustomConversions conversions = new ElasticsearchCustomConversions(Collections.emptyList());
            SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
            mappingContext.setInitialEntitySet(new EntityScanner(SpringUtil.getContext()).scan(Document.class));
            mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());

            MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);
            converter.setConversions(conversions);
            ElasticsearchTemplate template = new ElasticsearchTemplate(client,converter);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(ElasticsearchTemplate.class).setBean(template));


        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
    private RestClient createRestClient(ElasticsearchProperties cfg) {
        RestClientBuilder builder = RestClient.builder(cfg.getUris().stream().map(ElasticsearchAutoConfig::createNode).toList()
                .stream()
                .map((node) -> new HttpHost(node.getHost(), node.getPort(), node.getScheme()))
                .toArray(HttpHost[]::new));
        PropertyMapper map = PropertyMapper.get();
        builder.setHttpClientConfigCallback((httpClientBuilder) -> {
            httpClientBuilder.setDefaultCredentialsProvider(new CredentialsProvider(cfg));
            map.from(cfg::isSocketKeepAlive)
                    .to((keepAlive) -> httpClientBuilder
                            .setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(keepAlive).build()));

            configureSsl(httpClientBuilder,getBundle(cfg.getRestclient().getSsl().getBundle()));
            return httpClientBuilder;
        });
        builder.setRequestConfigCallback((requestConfigBuilder) -> {
            map.from(cfg::getConnectionTimeout)
                    .whenNonNull()
                    .asInt(Duration::toMillis)
                    .to(requestConfigBuilder::setConnectTimeout);
            map.from(cfg::getSocketTimeout)
                    .whenNonNull()
                    .asInt(Duration::toMillis)
                    .to(requestConfigBuilder::setSocketTimeout);
            return requestConfigBuilder;
        });
        String pathPrefix = cfg.getPathPrefix();
        if (StringUtils.isNotBlank(pathPrefix)) {
            builder.setPathPrefix(pathPrefix);
        }
//        SnifferBuilder builder = Sniffer.builder(client);
//        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
//        Duration interval = cfg.getRestclient().getSniffer().getInterval();
//        map.from(interval).asInt(Duration::toMillis).to(builder::setSniffIntervalMillis);
//        Duration delayAfterFailure = cfg.getRestclient().getSniffer().getDelayAfterFailure();
//        map.from(delayAfterFailure).asInt(Duration::toMillis).to(builder::setSniffAfterFailureDelayMillis);
        return builder.build();
    }
    private static URI createNode(String uri) {
        if (!(uri.startsWith("http://") || uri.startsWith("https://"))) {
            uri = "http://" + uri;
        }
        return URI.create(uri);
    }
    private static void configureSsl(HttpAsyncClientBuilder httpClientBuilder, SslBundle sslBundle) {
        if(sslBundle == null) {
            return;
        }
        SSLContext sslcontext = sslBundle.createSslContext();
        SslOptions sslOptions = sslBundle.getOptions();
        httpClientBuilder.setSSLStrategy(new SSLIOSessionStrategy(sslcontext, sslOptions.getEnabledProtocols(),
                sslOptions.getCiphers(), (HostnameVerifier) null));
    }
    private static class CredentialsProvider extends BasicCredentialsProvider {

        CredentialsProvider(ElasticsearchProperties cfg) {
            String username = cfg.getUsername();
            if (StringUtils.isNotBlank(username)) {
                setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, cfg.getPassword()));
            }
            Stream<URI> uris = cfg.getUris().stream().map(ElasticsearchAutoConfig::createNode);
            uris.filter(this::hasUserInfo).forEach(this::addUserInfoCredentials);
        }

        private boolean hasUserInfo(URI uri) {
            return StringUtils.isNotBlank(uri.getUserInfo());
        }

        private void addUserInfoCredentials(URI uri) {
            AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
            Credentials credentials = createUserInfoCredentials(uri.getUserInfo());
            setCredentials(authScope, credentials);
        }

        private Credentials createUserInfoCredentials(String userInfo) {
            int delimiter = userInfo.indexOf(":");
            if (delimiter == -1) {
                return new UsernamePasswordCredentials(userInfo, null);
            }
            String username = userInfo.substring(0, delimiter);
            String password = userInfo.substring(delimiter + 1);
            return new UsernamePasswordCredentials(username, password);
        }

    }
    // spring自带的依然符合装配条件，应该会自动生效
//    @Configuration
//    @Import(ElasticsearchRepositoriesAutoConfiguration.class)
//    public class RepositoriesAutoConfiguration {
//
//    }

}
