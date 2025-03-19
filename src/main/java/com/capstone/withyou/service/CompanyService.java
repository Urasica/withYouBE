package com.capstone.withyou.service;

import com.capstone.withyou.dao.Company;
import com.capstone.withyou.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ChatGptService chatGptService;

    // 회사 설명 조회
    public String getCompanyDescription(String companyName) {
        return companyRepository.findByCompanyName(companyName)
                .map(Company::getCompanyDescription) //회사 정보가 DB에 존재하면 설명 반환
                .orElseGet(()->{
                    // 회사 정보가 DB에 없다면 저장하고 설명 반환
                    String companyDescription = chatGptService.fetchCompanyDescription(companyName);
                    companyRepository.save(
                            new Company(companyName, companyDescription));
                    return companyDescription;
                });
    }
}
