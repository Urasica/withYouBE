package com.capstone.withyou.controller;

import com.capstone.withyou.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = "application/json; charset=UTF-8")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // 회사 정보 조회
    @GetMapping("/companies/{companyName}")
    public ResponseEntity<String> getCompanyDescription(@PathVariable String companyName) {
        String description = companyService.getCompanyDescription(companyName);

        return ResponseEntity.ok().body(description);
    }
}
