package com.qbit.exchanger.auth;

import com.qbit.exchanger.env.Env;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alexander_Sergeev
 */
public class AuthFilter implements Filter {

	public static final String USER_ID_KEY = "user_id";

	private Env env;

	@Override
	public void init(FilterConfig fc) throws ServletException {
		env = new Env();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		String userId = (String) httpRequest.getSession().getAttribute(USER_ID_KEY);

		boolean isRequestToAdminPage = (httpRequest.getRequestURI().equals("/exchanger/admin.jsp")
				|| ((httpRequest.getPathInfo() != null) && httpRequest.getPathInfo().startsWith("/admin")));
		boolean isAdmin = env.getAdminMail().equals(EncryptionUtil.getMD5(userId));
		boolean isAuthRequest = ((httpRequest.getPathInfo() == null) 
				|| ((httpRequest.getPathInfo() != null)
				&& (httpRequest.getPathInfo().equals("/")
				|| httpRequest.getPathInfo().startsWith("/users")
				|| httpRequest.getPathInfo().startsWith("/oauth2")
				|| httpRequest.getPathInfo().startsWith("/captcha-auth"))));
		
		if (isRequestToAdminPage && !isAdmin) {
			((HttpServletResponse) servletResponse).sendRedirect("");
		} else if ((userId == null) && !isAuthRequest) {
			((HttpServletResponse) servletResponse).sendRedirect("");
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
	}
}
