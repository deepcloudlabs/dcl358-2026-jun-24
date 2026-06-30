package com.example.crm.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crm.dto.CustomerDocumentDto;
import com.example.crm.service.CustomerQueryService;

@RestController
@RequestMapping("/customers")
@CrossOrigin
@Validated
public class CustomerQueryRestController {
    private final CustomerQueryService customerQueryService;

    public CustomerQueryRestController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    @GetMapping("{identity}")
    public CustomerDocumentDto getCustomerById(@PathVariable String identity) {
        return customerQueryService.findById(identity);
    }

    @GetMapping(params = { "pageNo", "pageSize" })
    public List<CustomerDocumentDto> getCustomers(@RequestParam int pageNo, @RequestParam int pageSize) {
        return customerQueryService.findAllByPage(pageNo, pageSize);
    }
}
