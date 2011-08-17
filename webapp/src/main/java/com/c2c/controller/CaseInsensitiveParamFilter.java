package com.c2c.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CaseInsensitiveParamFilter implements Filter {

	private static class CaseInsensitiveParamWrapper extends
			HttpServletRequestWrapper {

		public CaseInsensitiveParamWrapper(HttpServletRequest request) {
			super(request);
		}
		
		@Override
		public String getParameter(String name) {
			
			Enumeration names = super.getParameterNames();
			while (names.hasMoreElements()) {
				String realName = (String) names.nextElement();
				if (realName.equalsIgnoreCase(name)) {
					return super.getParameter(realName);
				}
			}
			return null;
		}
		
		@Override
		public Enumeration getParameterNames() {
			
			Enumeration names = super.getParameterNames();
			List newNames = new ArrayList();
			while (names.hasMoreElements()) {
				String realName = (String) names.nextElement();
				newNames.add(realName.toUpperCase());
			}
			
			return Collections.enumeration(newNames);
		}
		
		@Override
		public String[] getParameterValues(String name) {
			
			Enumeration names = super.getParameterNames();
			while (names.hasMoreElements()) {
				String realName = (String) names.nextElement();
				if (realName.equalsIgnoreCase(name)) {
					return super.getParameterValues(realName);
				}
			}
			return null;
		}
		
		@Override
		public Map getParameterMap() {

			Enumeration names = super.getParameterNames();
			Map newNames = new HashMap();
			while (names.hasMoreElements()) {
				String realName = (String) names.nextElement();
				newNames.put(realName.toUpperCase(), super.getParameterValues(realName));
			}
			
			return newNames;
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// override the request passed to the FilterChain
		HttpServletRequest overridenRequest = new CaseInsensitiveParamWrapper(
				(HttpServletRequest) request);
		chain.doFilter(overridenRequest, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}
}