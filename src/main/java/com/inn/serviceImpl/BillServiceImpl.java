package com.inn.serviceImpl;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import com.inn.service.CartService;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.inn.JWT.JwtFilter;
import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;
import com.inn.POJO.Product;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.BillDao;
import com.inn.dao.ProductDao;
import com.inn.service.BillService;
import com.inn.service.OrderStatusService;
import com.inn.utils.TaphoaUtils;

import com.inn.config.RabbitMQConfig;
import com.inn.config.VNPayConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public String createOrder(int total, String orderInfor, String urlReturn){
        Bill bill = billDao.findByUuid(orderInfor);
        if (bill == null) {
            throw new RuntimeException("Bill not found");
        }
        if (bill.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new RuntimeException("Order is not ready for payment");
        }
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total*100));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        urlReturn += VNPayConfig.vnp_Returnurl;
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    @Override
    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    @Override
    public ResponseEntity<?> generateReport(Map<String, Object> requestMap) {
        try {
            if (!validateRequestMap(requestMap)) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Thiếu dữ liệu bắt buộc")
                );
            }

            String fileName;

            if (requestMap.containsKey("isGenerate")
                    && Boolean.FALSE.equals(requestMap.get("isGenerate"))) {

                if (!requestMap.containsKey("uuid")) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Thiếu UUID")
                    );
                }

                fileName = requestMap.get("uuid").toString();

            } else {
                if (!requestMap.containsKey("productDetails")) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Thiếu danh sách sản phẩm")
                    );
                }

                // Stock validation moved to RabbitMQ
                fileName = TaphoaUtils.getUUID();
                requestMap.put("uuid", fileName);

                try {
                    insertBill(requestMap);
                } catch (Exception e) {
                    log.error("Insert bill failed", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            Map.of("message", "Không thể tạo hoá đơn")
                    );
                }
            }

            Document document = new Document();
            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(
                            TaphoaConstants.STORE_LOCATION + "\\" + fileName + ".pdf"
                    )
            );

            document.open();
            setRectangleInPdf(document);

            Paragraph header = new Paragraph(
                    "Taphoa Management System",
                    getFont("Header")
            );
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            String data =
                    "Name:" + requestMap.get("name") + "\n" +
                            "Contact Number:" + requestMap.get("contactNumber") + "\n" +
                            "Email:" + requestMap.get("email") + "\n" +
                            "Payment Method:" + requestMap.get("paymentMethod");

            document.add(new Paragraph(data + "\n\n", getFont("Data")));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table);

            JSONArray jsonArray = TaphoaUtils.getJsonArrayFromString(
                    requestMap.get("productDetails").toString()
            );

            for (int i = 0; i < jsonArray.length(); i++) {
                addRows(
                        table,
                        TaphoaUtils.getMapFromJson(jsonArray.getString(i))
                );
            }

            document.add(table);

            document.add(
                    new Paragraph(
                            "Total: " + requestMap.get("totalAmount")
                                    + "\nThank you for visiting. Please visit again!!",
                            getFont("Data")
                    )
            );

            document.close();

            return ResponseEntity.ok(
                    Map.of("uuid", fileName)
            );

        } catch (Exception ex) {
            log.error("Generate report error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Lỗi tạo hoá đơn")
            );
        }
    }

    private void addRows(PdfPTable table, Map<String,Object> data) {
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type){
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        Rectangle rect = new Rectangle(577, 825, 18, 15);
        rect.enableBorderSide(1);
        rect.enableBorderSide(2);
        rect.enableBorderSide(4);
        rect.enableBorderSide(8);
        rect.setBorderColor(BaseColor.BLACK);
        rect.setBorderWidth(1);
        document.add(rect);
    }

    private void insertBill(Map<String,Object> requestMap) {
        try{
            Bill bill = new Bill();
            bill.setUuid((String)requestMap.get("uuid"));
            bill.setName((String)requestMap.get("name"));
            bill.setEmail((String)requestMap.get("email"));
            bill.setContactNumber((String)requestMap.get("contactNumber"));
            bill.setPaymentMethod((String)requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String)requestMap.get("totalAmount")));
            bill.setProductDetail((String)requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            bill.setCreatedDate(LocalDateTime.now());
            bill.setUpdatedDate(LocalDateTime.now());

            // Set initial status
            bill.setStatus(OrderStatus.CONFIRMING);
            billDao.save(bill);
            cartService.clearCart();

            // Send to RabbitMQ for stock validation and deduction
            Map<String, Object> message = new HashMap<>();
            message.put("orderId", bill.getId());
            message.put("productDetails", bill.getProductDetail());
            message.put("paymentMethod", (String)requestMap.get("paymentMethod"));
            message.put("action", "process_order");
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_QUEUE, message);

        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * Xác minh số lượng cho tất cả các mặt hàng trong đơn hàng
     * Thông báo lỗi nếu không có đủ hàng, null nếu tất cả các mặt hàng đều đủ
     **/
    private ResponseEntity<?> validateProductStock(String productDetailsJson) {
        try {
            JSONArray jsonArray = TaphoaUtils.getJsonArrayFromString(productDetailsJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> productData =
                        TaphoaUtils.getMapFromJson(jsonArray.getString(i));

                log.info("ProductData = {}", productData);

                Object idObj = productData.get("id");
                Object qtyObj = productData.get("quantity");

                if (idObj == null || qtyObj == null) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Dữ liệu sản phẩm không hợp lệ")
                    );
                }

                Integer productId = (idObj instanceof Number)
                        ? ((Number) idObj).intValue()
                        : Integer.parseInt(idObj.toString());

                Integer requestedQuantity = (qtyObj instanceof Number)
                        ? ((Number) qtyObj).intValue()
                        : Integer.parseInt(qtyObj.toString());

                Optional<Product> productOpt = productDao.findById(productId);
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Sản phẩm không tồn tại", "productId", productId)
                    );
                }

                Product product = productOpt.get();

                if (product.getQuantity() == null || product.getQuantity() < requestedQuantity) {
                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "message", "Số lượng vượt quá tồn kho",
                                    "productId", productId,
                                    "available", product.getQuantity() != null ? product.getQuantity() : 0
                            )
                    );
                }
            }
            return null;

        } catch (Exception ex) {
            log.error("Error validating product stock", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Lỗi kiểm tra tồn kho")
            );
        }
    }


    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productById", allEntries = true)
    })
    //Trừ vào số lượng mặt hàng trong kho sau khi thanh toán thành công
    private void deductProductStock(String productDetailsJson) {
        try {
            JSONArray jsonArray = TaphoaUtils.getJsonArrayFromString(productDetailsJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> productData = TaphoaUtils.getMapFromJson(jsonArray.getString(i));

                Object idObj = productData.get("id");
                Object qtyObj = productData.get("quantity");

                Integer productId = (idObj instanceof Number)
                        ? ((Number) idObj).intValue()
                        : Integer.parseInt(idObj.toString());

                Integer quantity = (qtyObj instanceof Number)
                        ? ((Number) qtyObj).intValue()
                        : Integer.parseInt(qtyObj.toString());

                Optional<Product> productOpt = productDao.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    if (product.getQuantity() != null) {
                        product.setQuantity(product.getQuantity() - quantity);
                        productDao.save(product);
                        log.info("Product {} stock reduced by {}. New stock: {}", productId, quantity, product.getQuantity());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error deducting product stock", ex);
        }
    }

    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "productsByCategory", allEntries = true),
        @CacheEvict(value = "productById", allEntries = true)
    })
    public void deductStock(String productDetailsJson) {
        deductProductStock(productDetailsJson);
    }

    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "productsByCategory", allEntries = true),
        @CacheEvict(value = "productById", allEntries = true)
    })
    public void addStockBack(String productDetailsJson) {
        try {
            JSONArray jsonArray = TaphoaUtils.getJsonArrayFromString(productDetailsJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> productData = TaphoaUtils.getMapFromJson(jsonArray.getString(i));

                Object idObj = productData.get("id");
                Object qtyObj = productData.get("quantity");

                Integer productId = (idObj instanceof Number)
                        ? ((Number) idObj).intValue()
                        : Integer.parseInt(idObj.toString());

                Integer quantity = (qtyObj instanceof Number)
                        ? ((Number) qtyObj).intValue()
                        : Integer.parseInt(qtyObj.toString());

                Optional<Product> productOpt = productDao.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    if (product.getQuantity() != null) {
                        product.setQuantity(product.getQuantity() + quantity);
                        productDao.save(product);
                        log.info("Product {} stock added back by {}. New stock: {}", productId, quantity, product.getQuantity());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error adding stock back", ex);
        }
    }


    private boolean validateRequestMap(Map<String,Object> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("paymentMethod")
                && requestMap.containsKey("productDetails") && requestMap.containsKey("totalAmount");
    }


    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = new ArrayList<>();
        if(jwtFilter.isAdmin()){
            list = billDao.getAllBills();
        }else{
            list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public void handlePaymentResult(int paymentStatus, String uuid) {
        try {
            Bill bill = billDao.findByUuid(uuid);
            if (bill == null) {
                log.error("Bill not found for uuid: {}", uuid);
                return;
            }
            if (paymentStatus == 1) {
                // Update status to preparing shipment
                orderStatusService.updateOrderStatus(bill.getId(), OrderStatus.PREPARING_SHIPMENT);
            } else {
                // Payment failed, add stock back and cancel
                addStockBack(bill.getProductDetail());
                orderStatusService.updateOrderStatus(bill.getId(), OrderStatus.CANCELLED);
            }
        } catch (Exception e) {
            log.error("Error handling payment result", e);
        }
    }

    @Override
    public ResponseEntity<List<Bill>> getUserBills() {
        List<Bill> list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    public synchronized boolean validateStock(Integer orderId, String productDetails) {
        try {
            // Validate stock
            ResponseEntity<?> stockError = validateProductStock(productDetails);
            if (stockError != null) {
                log.error("Stock validation failed for order {}", orderId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating stock for order {}", orderId, e);
            return false;
        }
    }

    public synchronized void deductStockForOrder(Integer orderId) {
        try {
            Bill bill = billDao.findById(orderId).orElse(null);
            if (bill != null) {
                deductStock(bill.getProductDetail());
                log.info("Stock deducted for successful payment of order {}", orderId);
            } else {
                log.error("Bill not found for order {}", orderId);
            }
        } catch (Exception e) {
            log.error("Error deducting stock for order {}", orderId, e);
        }
    }

    @Override
    public ResponseEntity<String> cancelOrder(Integer id) {
        try {
            Optional<Bill> billOpt = billDao.findById(id);
            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();
                // Add stock back if order was already processed
                if (bill.getStatus() == OrderStatus.PREPARING_SHIPMENT || bill.getStatus() == OrderStatus.AWAITING_PAYMENT) {
                    addStockBack(bill.getProductDetail());
                }
                orderStatusService.updateOrderStatus(id, OrderStatus.CANCELLED);
            }
            return TaphoaUtils.getResponseEntity("Order cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<String> updateOrderStatus(Integer id, OrderStatus newStatus) {
        try {
            orderStatusService.updateOrderStatus(id, newStatus);
            return TaphoaUtils.getResponseEntity("Order status updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf : requestMap {}", requestMap);
        try{
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)){
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = TaphoaConstants.STORE_LOCATION + "\\"+(String)requestMap.get("uuid")+".pdf";
            if (TaphoaUtils.isFileExist(filePath)){
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }else{
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
        }catch (Exception ex){
            log.error("Exception occurred while getting pdf", ex);
        }
        return null;
    }

    private byte[] getByteArray(String filePath) throws Exception{
        File initialFile = new File(filePath);
        InputStream targetStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }


    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            if(jwtFilter.isAdmin()){
                Optional<Bill> optional = billDao.findById(id);
                if(optional.isPresent()){
                    billDao.deleteById(id);
                    return TaphoaUtils.getResponseEntity("Bill Deleted Successfully", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Bill id does not exist", HttpStatus.OK);
            }else{
                return TaphoaUtils.getResponseEntity("You are not authorized to perform this operation", HttpStatus.FORBIDDEN);
            }
        }catch (Exception ex){
            log.error("Exception occurred while deleting bill", ex);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getBillStatus(String uuid) {
        try {
            Bill bill = billDao.findByUuid(uuid);
            if (bill != null) {
                return ResponseEntity.ok(Map.of("status", bill.getStatus()));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Exception occurred while getting bill status", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}