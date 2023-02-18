/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package scouter.agent.proxy;

import scouter.agent.Logger;
import scouter.agent.trace.TraceContext;

import java.lang.reflect.Method;
import java.util.Enumeration;

public class HttpTraceFactory {
	private static final String HTTP_TRACE = "scouter.xtra.http.HttpTrace";
	private static final String HTTP_TRACE3 = "scouter.xtra.http.HttpTrace3";
	private static final String JAKARTA_HTTP_TRACE3 = "scouter.xtra.http.HttpTraceJakarta3";
	private static final String HTTP_TRACE_WEBFLUX = "scouter.xtra.http.WebfluxHttpTrace";

	public static final IHttpTrace dummy = new IHttpTrace() {
		public String getParameter(Object req, String key) {
			return null;
		}
		public String getHeader(Object req, String key) {
			return null;
		}
		public String getCookie(Object req, String key) {
			return null;
		}
		public String getRequestURI(Object req) {
			return null;
		}
		public String getRequestId(Object req) {
			return null;
		}
		public String getRemoteAddr(Object req) {
			return null;
		}
		public String getMethod(Object req) {
			return null;
		}
		public String getQueryString(Object req) {
			return null;
		}
		public Object getAttribute(Object req, String key) {
			return null;
		}
		public Enumeration getParameterNames(Object req) {
			return null;
		}
		public Enumeration getHeaderNames(Object req) {
			return null;
		}
		public Object subscriptOnContext(Object mono0, TraceContext traceContext) {
			return mono0;
		}
		public void start(TraceContext ctx, Object req, Object res) {
		}
		public void end(TraceContext ctx, Object req, Object res) {
		}
		public void rejectText(Object res, String text) {
		}
		public void rejectUrl(Object res, String url) {
		}
		public void addAsyncContextListener(Object ac) {
		}
		public TraceContext getTraceContextFromAsyncContext(Object oAsyncContext) {
			return null;
		}
		public void setDispatchTransferMap(Object oAsyncContext, long gxid, long caller, long callee, byte xType) {
		}
		public void setSelfDispatch(Object oAsyncContext, boolean self) {
		}
		public boolean isSelfDispatch(Object oAsyncContext) {
			return false;
		}
		public void contextOperatorHook() {
		}
	};

	public static IHttpTrace create(ClassLoader parent, Object oReq) {
		try {
			ClassLoader loader = LoaderManager.getHttpLoader(parent);
			if (loader == null) {
				return dummy;
			}

			Class c = null;

			boolean reactive = true;
			boolean jakarta = false;
			try {
				Method m = oReq.getClass().getMethod("mutate");
				c = Class.forName(HTTP_TRACE_WEBFLUX, true, loader);

			} catch (Exception e) {
				reactive = false;
			}

			try {
				for (Method method : oReq.getClass().getMethods()) {
					if (method.toString().contains("jakarta")) {
						jakarta = true;
						break;
					}
				}
			} catch (Exception e) {
				Logger.println("A134", "fail to create jakarta", e);
			}

			if (jakarta) {
				c = Class.forName(JAKARTA_HTTP_TRACE3, true, loader);
			} else if (!reactive) {
				boolean servlet3 = true;
				try {
					Method m = oReq.getClass().getMethod("logout");
				} catch (Exception e) {
					servlet3 = false;
				}

				if(servlet3) {
					c = Class.forName(HTTP_TRACE3, true, loader);
				} else {
					c = Class.forName(HTTP_TRACE, true, loader);
				}
			}

			return (IHttpTrace) c.newInstance();
		} catch (Throwable e) {
			Logger.println("A133", "fail to create", e);
			return dummy;
		}
	}
}
