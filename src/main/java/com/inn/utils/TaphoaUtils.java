package com.inn.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import com.google.common.reflect.TypeToken;

@Slf4j
public class TaphoaUtils {
    private TaphoaUtils() {}

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body("{\"message\":\"" + responseMessage + "\"}");
    }

    public static String getUUID() {
        Date date = new Date();
        long time = date.getTime();
        return "BILL-" + time;
    }

    public static JSONArray getJsonArrayFromString(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        return jsonArray;
    }

    public static Map<String, Object> getMapFromJson(String data) {
        if (!Strings.isNullOrEmpty(data)) {
            return new Gson().fromJson(data, new TypeToken<Map<String, Object>>() {
            }.getType());
        }
        return new HashMap<>();
    }

    public static boolean isFileExist(String path) {
        log.info("Inside isFileExist {}", path);
        try {
            if (path == null) return false;
            File file = new File(path);
            return file.exists();
        } catch (Exception ex) {
            log.error("Exception in isFileExist", ex);
        }
        return false;
    }
}
