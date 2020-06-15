package uk.gov.justice.digital.delius.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;

@Configuration
public class DeliusConfig {

    @Bean("deliusWebClientWithAuth")
    public WebClient deliusWebClientWithAuth(final WebClient.Builder builder,
                                                @Value("${delius.baseurl}") final String baseUrl,
                                                @Value("${delius.username}") final String deliusUsername,
                                                @Value("${delius.password}") final String deliusPassword,
                                                @Value("${delius.connectTimeout:30s}") final Duration connectTimeout,
                                                @Value("${delius.readTimeout:30s}") final Duration readTimeout) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeaders(header -> header.setBasicAuth(deliusUsername, deliusPassword))
                .clientConnector(getClientConnectorWithTimeouts(connectTimeout, readTimeout))
                .build();
    }

    private ClientHttpConnector getClientConnectorWithTimeouts(final Duration connectTimeout, final Duration readTimeout) {
        final var tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Long.valueOf(connectTimeout.toMillis()).intValue())
                .doOnConnected(connection ->
                        connection
                        .addHandlerLast(new ReadTimeoutHandler(Long.valueOf(readTimeout.toSeconds()).intValue()))
                );
        return new ReactorClientHttpConnector(HttpClient.from(tcpClient));
    }
}
