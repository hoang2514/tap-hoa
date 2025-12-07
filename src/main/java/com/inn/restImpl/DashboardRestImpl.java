package com.inn.restImpl;

import com.inn.rest.DashboardRest;
import com.inn.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class DashboardRestImpl implements DashboardRest {

    @Autowired
    private DashboardService dashboardService;

    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        log.info("Inside getCount method of DashboardRestImpl");
        try {
            return dashboardService.getCount();
        } catch (Exception ex) {
            log.error("Exception in getCount", ex);
            return ResponseEntity.internalServerError().body(Map.of("message", "Something went wrong while fetching counts"));
        }
    }
}