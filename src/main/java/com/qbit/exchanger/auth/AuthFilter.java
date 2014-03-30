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

	@Override
	public void init(FilterConfig fc) throws ServletException {
		env = new Env();
	}

	@Override
	public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sr;
		String userId = (String) req.getSession().getAttribute("user_id");
		if (userId != null) {
			if (req.getRequestURI().equals("/exchanger/admin.jsp")
					|| (req.getPathInfo() != null && req.getPathInfo().startsWith("/admin"))) {
				if (!userId.equals(env.getGoogleUserId())) {
					req.getRequestDispatcher("/webapi/index").forward(sr, sr1);
				}
			}
		} else if (req.getPathInfo() == null || !req.getPathInfo().equals("/oauth2/authorize")) {
			req.getRequestDispatcher("/webapi/oauth2/authenticate").forward(sr, sr1);
		}
		fc.doFilter(sr, sr1);
	}

	@Override
	public void destroy() {
	}
}
