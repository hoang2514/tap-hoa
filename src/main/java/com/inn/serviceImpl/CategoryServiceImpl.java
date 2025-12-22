package com.inn.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.inn.JWT.JwtFilter;
import com.inn.POJO.Category;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.CategoryDao;
import com.inn.service.CategoryService;
import com.inn.utils.TaphoaUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, false)){
                    categoryDao.save(getCategoryFromMap(requestMap, false));
                    return TaphoaUtils.getResponseEntity("Category added successfully", HttpStatus.OK);
                }
                }else{
                    return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        }
    }catch(Exception ex){
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    
}

    private boolean validateCategoryMap(Map<String,String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")){
            if(requestMap.containsKey("id") && validateId){
                return true;
            }
            else if(!validateId){
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Category category = new Category();
        if(isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try{
            if(StringUtils.hasText(filterValue) && filterValue.equalsIgnoreCase("true")) {
                return new ResponseEntity<List<Category>>(categoryDao.getAllCategory(), HttpStatus.OK);
            }
            return new ResponseEntity<List<Category>>(categoryDao.findAll(), HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, true)){
                    Optional<Category> optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(optional.isPresent()){
                        categoryDao.save(getCategoryFromMap(requestMap, true));
                        return TaphoaUtils.getResponseEntity("Category Updated Successfully", HttpStatus.OK);
                    }else{
                        return TaphoaUtils.getResponseEntity("Category id doesn't not exist", HttpStatus.OK);
                    }
                }
                return TaphoaUtils.getResponseEntity(TaphoaConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else{
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCategory(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Category> optional = categoryDao.findById(id);
                if (optional.isPresent()) {
                    categoryDao.deleteById(id);
                    return TaphoaUtils.getResponseEntity("Category deleted successfully", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Category id does not exist", HttpStatus.OK);
            }
            return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}