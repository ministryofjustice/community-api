package uk.gov.justice.digital.delius.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import uk.gov.justice.digital.delius.utils.UserContext;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;

@Configuration
public class DeliusApiConfig {

    private ClientHttpConnector getClientConnectorWithTimeouts(final Duration connectTimeout, final Duration readTimeout) {
        final var tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Long.valueOf(connectTimeout.toMillis()).intValue())
            .doOnConnected(connection ->
                connection
                    .addHandlerLast(new ReadTimeoutHandler(Long.valueOf(readTimeout.toSeconds()).intValue()))
            );
        return new ReactorClientHttpConnector(HttpClient.from(tcpClient));
    }

    @Bean("deliusApiWebClient")
    WebClient webClient(final WebClient.Builder builder, @Value("${deliusApi.baseurl}") final String baseUrl,
                        @Value("${deliusApi.connectTimeout:30s}") final Duration connectTimeout,
                        @Value("${deliusApi.readTimeout:30s}") final Duration readTimeout) {
        return builder
            .baseUrl(baseUrl)
            .filter(addAuthHeaderFilterFunction())
            .clientConnector(getClientConnectorWithTimeouts(connectTimeout, readTimeout))
            .build();
    }

    @NotNull
    private ExchangeFilterFunction addAuthHeaderFilterFunction() {
        return (request, next) -> {
            ClientRequest filtered = ClientRequest.from(request)
                .header(HttpHeaders.AUTHORIZATION, UserContext.getAuthToken())
                .build();
            return next.exchange(filtered);
        };
    }
}
