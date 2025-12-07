package com.inn.serviceImpl;

import com.inn.dao.BillDao;
import com.inn.dao.CategoryDao;
import com.inn.dao.ProductDao;
import com.inn.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private BillDao billDao;

    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        log.info("Inside getCount method of DashboardServiceImpl");
        Map<String, Object> responseMap = new HashMap<>();
        try {
            responseMap.put("category", categoryDao.count());
            responseMap.put("product", productDao.count());
            responseMap.put("bill", billDao.count());
            log.info("Counts fetched successfully: {}", responseMap);
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Exception occurred while fetching counts", ex);
            responseMap.put("message", "Something went wrong while fetching counts");
            return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}