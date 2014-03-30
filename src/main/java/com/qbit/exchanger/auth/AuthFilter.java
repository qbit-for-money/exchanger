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

/**
 * @author Alexander_Sergeev
 */
public class AuthFilter implements Filter {

	private Env env;
        public static final String USER_ID = "user_id";
	@Override
	public void init(FilterConfig fc) throws ServletException {
		env = new Env();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		String userId = (String) httpRequest.getSession().getAttribute(USER_ID);

		boolean isRequestToAdminPage = (userId != null 
				&& (httpRequest.getRequestURI().equals("/exchanger/admin.jsp")
				|| ((httpRequest.getPathInfo() != null) && httpRequest.getPathInfo().startsWith("/admin"))));
		
		boolean isRequestToOther = userId == null 
				&& (httpRequest.getPathInfo() == null || !httpRequest.getPathInfo().equals("/oauth2/authorize"));
		
		boolean isAdmin = (userId != null) ? userId.equals(env.getAdminMail()) : false;

		if (isRequestToAdminPage && !isAdmin) {
			httpRequest.getRequestDispatcher("/webapi/index").forward(servletRequest, servletResponse);
		} else if (isRequestToOther) {
			httpRequest.getRequestDispatcher("/webapi/oauth2/authenticate").forward(servletRequest, servletResponse);
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {
	}
}
