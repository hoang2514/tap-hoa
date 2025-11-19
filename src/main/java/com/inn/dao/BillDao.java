package com.inn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.POJO.Bill;

public interface BillDao extends JpaRepository<Bill, Integer> {
    
}
