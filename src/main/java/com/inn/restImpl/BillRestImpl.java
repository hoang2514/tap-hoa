package com.inn.restImpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;
import com.inn.constants.TaphoaConstants;
import com.inn.rest.BillRest;
import com.inn.service.BillService;
import com.inn.utils.TaphoaUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class BillRestImpl implements BillRest {

    @Autowired
    private BillService billService;

    @Override
    public ResponseEntity<?> generateReport(Map<String, Object> requestMap) {
        try {
            return billService.generateReport(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        log.info("Inside getBills");
        try {
            return billService.getBills();
        } catch (Exception ex) {
            log.error("Exception in getBills", ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf");
        try {
            return billService.getPdf(requestMap);
        } catch (Exception ex) {
            log.error("Exception in getPdf", ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> deleteBill(Integer id) {
        log.info("Inside deleteBill with ID: {}", id);
        try {
            return billService.deleteBill(id);
        } catch (Exception ex) {
            log.error("Exception in deleteBill", ex);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);


    }

    public ResponseEntity<List<Bill>> getUserBills() {
        log.info("Inside getUserBills");
        try {
            return billService.getUserBills();
        } catch (Exception ex) {
            log.error("Exception in getUserBills", ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> cancelOrder(Integer id) {
        log.info("Inside cancelOrder with ID: {}", id);
        try {
            return billService.cancelOrder(id);
        } catch (Exception ex) {
            log.error("Exception in cancelOrder", ex);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateOrderStatus(Integer id, Map<String, Object> requestMap) {
        log.info("Inside updateOrderStatus with ID: {}", id);
        try {
            String statusStr = (String) requestMap.get("status");
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            return billService.updateOrderStatus(id, newStatus);
        } catch (Exception ex) {
            log.error("Exception in updateOrderStatus", ex);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getBillStatus(String uuid) {
        log.info("Inside getBillStatus with UUID: {}", uuid);
        try {
            return billService.getBillStatus(uuid);
        } catch (Exception ex) {
            log.error("Exception in getBillStatus", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
