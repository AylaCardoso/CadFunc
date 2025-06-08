package com.empresa.filters;

import jakarta.servlet.*;
import java.io.IOException;

public class CharacterEncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) {
        String configEncoding = filterConfig.getInitParameter("encoding");
        if (configEncoding != null) {
            encoding = configEncoding;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=" + encoding);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
