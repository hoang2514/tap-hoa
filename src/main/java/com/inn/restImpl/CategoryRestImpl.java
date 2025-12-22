package com.inn.restImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.POJO.Category;
import com.inn.constants.TaphoaConstants;
import com.inn.rest.CategoryRest;
import com.inn.service.CategoryService;
import com.inn.utils.TaphoaUtils;

@RestController
public class CategoryRestImpl implements CategoryRest {

@Autowired
CategoryService categoryService;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        
        try{
            return categoryService.addNewCategory(requestMap);

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        
        try{
            return categoryService.getAllCategory(filterValue);

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try{
            return categoryService.updateCategory(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCategory(Integer id) {
        try {
            return categoryService.deleteCategory(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
