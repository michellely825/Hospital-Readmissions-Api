// Servlet filter that logs memory usage, connection pool stats, and request details
// for every request — used to identify which endpoints are causing the memory leak.

package com.michellely.hospital_api;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;

@Component
public class MemoryMonitoringFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MemoryMonitoringFilter.class);
    private static final long MB = 1024L * 1024L;

    private final DataSource dataSource;

    public MemoryMonitoringFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String method      = httpReq.getMethod();
        String path        = httpReq.getRequestURI();
        String queryString = httpReq.getQueryString();
        String fullPath    = queryString != null ? path + "?" + queryString : path;

        Runtime rt = Runtime.getRuntime();
        long totalBefore = rt.totalMemory();
        long freeBefore  = rt.freeMemory();
        long usedBefore  = totalBefore - freeBefore;

        long startNs = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;

            long totalAfter = rt.totalMemory();
            long freeAfter  = rt.freeMemory();
            long usedAfter  = totalAfter - freeAfter;
            long memDelta   = usedAfter - usedBefore;

            int status = httpResp.getStatus();

            // Connection pool stats (HikariCP-specific)
            String poolStats = getPoolStats();

            log.info(
                "[REQUEST] {} {} -> {} | time={}ms" +
                " | mem_before={}MB mem_after={}MB mem_delta={}{} MB" +
                " | heap_total={}MB" +
                " | pool=[{}]",
                method, fullPath, status, elapsedMs,
                usedBefore / MB, usedAfter / MB,
                memDelta >= 0 ? "+" : "", memDelta / MB,
                totalAfter / MB,
                poolStats
            );
        }
    }

    /**
     * Extracts active, idle, and pending counts from the HikariCP pool via
     * reflection on the internal HikariPool object. Falls back gracefully if
     * the pool implementation is not HikariCP or the fields are inaccessible.
     */
    private String getPoolStats() {
        if (!(dataSource instanceof HikariDataSource hikariDs)) {
            return "n/a (not HikariCP)";
        }

        try {
            // HikariDataSource exposes getHikariPoolMXBean() for basic stats,
            // but the richer HikariPool object (accessible via reflection) gives
            // us active + idle + pending in one shot.
            Field poolField = HikariDataSource.class.getDeclaredField("pool");
            poolField.setAccessible(true);
            HikariPool pool = (HikariPool) poolField.get(hikariDs);

            if (pool == null) {
                return "pool not yet initialised";
            }

            int active  = pool.getActiveConnections();
            int idle    = pool.getIdleConnections();
            int pending = pool.getThreadsAwaitingConnection();
            int total   = pool.getTotalConnections();

            return String.format("active=%d idle=%d pending=%d total=%d",
                    active, idle, pending, total);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Fall back to the public MXBean API which gives a subset of stats
            try {
                var mxBean = hikariDs.getHikariPoolMXBean();
                if (mxBean == null) {
                    return "pool not yet initialised";
                }
                return String.format("active=%d idle=%d pending=%d total=%d",
                        mxBean.getActiveConnections(),
                        mxBean.getIdleConnections(),
                        mxBean.getThreadsAwaitingConnection(),
                        mxBean.getTotalConnections());
            } catch (Exception ex) {
                return "unavailable: " + ex.getMessage();
            }
        }
    }
}
