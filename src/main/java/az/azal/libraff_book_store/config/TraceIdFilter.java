package az.azal.libraff_book_store.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

	private static final String TRACE_ID_HEADER = "X-Trace-Id";
	private static final String MDC_TRACE_ID_KEY = "traceId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1. Check if the client sent a trace ID, otherwise generate a new one
		String traceId = request.getHeader(TRACE_ID_HEADER);
		if (traceId == null || traceId.isEmpty()) {
			traceId = UUID.randomUUID().toString().substring(0, 8); // Short UUID for readability
		}

		// 2. Put it in MDC (this makes it available to Logback's %X{traceId})
		MDC.put(MDC_TRACE_ID_KEY, traceId);

		// 3. Optionally return it in the response header
		response.setHeader(TRACE_ID_HEADER, traceId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			// 4. IMPORTANT: Always clear MDC after the request to prevent memory leaks
			// and mixed-up logs in thread pools!
			MDC.remove(MDC_TRACE_ID_KEY);
		}
	}

}
