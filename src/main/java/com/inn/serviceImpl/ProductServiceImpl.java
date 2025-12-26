package com.inn.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.JWT.JwtFilter;
import com.inn.POJO.Category;
import com.inn.POJO.Product;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.ProductDao;
import com.inn.service.ProductService;
import com.inn.utils.TaphoaUtils;
import com.inn.wrapper.ProductWrapper;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    // Khi thêm mới: Xóa cache danh sách tổng và danh sách theo category
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true)
    })
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    productDao.save(getProductFromMap(requestMap, false));
                    return TaphoaUtils.getResponseEntity("Product added successfully.", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity(TaphoaConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Product getProductFromMap(Map<String,String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        Product product = new Product();
        if (isAdd) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Float.parseFloat(requestMap.get("price")));
        if (requestMap.containsKey("imageUrl")) {
            product.setImageUrl(requestMap.get("imageUrl"));
        }
        return product;
    }

    private boolean validateProductMap(Map<String,String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            }
            else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    @Override
    // Cache danh sách tổng. Tên cache: "products"
    @Cacheable(value = "products")
    public List<ProductWrapper> getAllProduct() {
        try {
            // Dòng này chỉ in ra nếu Cache CHƯA có dữ liệu (Cache Miss)
            System.out.println("--- LOG: Đang lấy dữ liệu từ Database (Không dùng Cache) ---");
            return productDao.getAllProduct();
        } catch (Exception e) {
            e.printStackTrace();
        } return new ArrayList<>();
    }

    @Override
    // Khi update: Xóa cache danh sách, cache category và cache CỤ THỂ của ID đó
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productById", key = "#requestMap.get('id')")
    })
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, true)) {
                    Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (!optional.isEmpty()) {
                        Product product = getProductFromMap(requestMap, true);
                        product.setStatus(optional.get().getStatus());
                        productDao.save(product);
                        return TaphoaUtils.getResponseEntity("Product updated successfully.", HttpStatus.OK);
                    } else {
                        return TaphoaUtils.getResponseEntity("Product id does not exist.", HttpStatus.OK);
                    }
                } else {
                    return TaphoaUtils.getResponseEntity(TaphoaConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    // Khi delete: Xóa hết các cache liên quan
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productById", key = "#id")
    })
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productDao.findById(id);
                if (!optional.isEmpty()) {
                    productDao.deleteById(id);
                    return TaphoaUtils.getResponseEntity("Product deleted successfully.", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Product does not exist", HttpStatus.OK);
            }
            return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity(TaphoaConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
        // return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    // Update Status cũng làm thay đổi dữ liệu hiển thị -> Xóa cache
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productById", key = "#requestMap.get('id')")
    })
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return TaphoaUtils.getResponseEntity("Product status updated successfully.", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Product id does not exist.", HttpStatus.OK);
            }
            return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    // Cache danh sách theo Category. Key sẽ là ID của category
    @Cacheable(value = "productsByCategory", key = "#id")
    public List<ProductWrapper> getByCategory(Integer id) {
        try {
            return productDao.getProductByCategory(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    // Cache chi tiết 1 product. Key là ID product
    @Cacheable(value = "productById", key = "#id")
    public ProductWrapper getProductById(Integer id) {
        try {
            return productDao.getProductById(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ProductWrapper();
    }
}