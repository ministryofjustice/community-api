package uk.gov.justice.digital.delius.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
@Order(4)
public class UserContextFilter implements Filter {
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {


        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        final String authToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        UserContext.setAuthToken(authToken);

        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
