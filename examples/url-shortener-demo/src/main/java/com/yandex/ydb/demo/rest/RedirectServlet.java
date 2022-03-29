package com.yandex.ydb.demo.rest;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yandex.ydb.demo.Application;
import com.yandex.ydb.demo.ydb.HashTool;
import com.yandex.ydb.demo.ydb.UrlRecord;
import com.yandex.ydb.demo.ydb.YdbException;
import com.yandex.ydb.demo.ydb.YdbRepository;
import org.eclipse.jetty.servlet.DefaultServlet;

/**
 *
 * @author Alexandr Gorshenin
 */
public class RedirectServlet extends DefaultServlet {
    private YdbRepository repository() {
        return new YdbRepository(Application.ydp());
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().length() > 1) {
            // Remove trailing '/'
            String hash = req.getRequestURI().substring(1);
            if (HashTool.isHash(hash)) {
                try {
                    Optional<UrlRecord> record = repository().findByHash(hash);
                    if (record.isPresent()) {
                        resp.setHeader("Location", record.get().url());
                        resp.setStatus(302);
                        return;
                    }
                } catch (YdbException e) {
                    throw new ServletException(e.getMessage(), e);
                }
            }
        }

        super.doGet(req, resp);
    }
}
