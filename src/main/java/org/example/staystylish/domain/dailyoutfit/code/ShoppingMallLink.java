package org.example.staystylish.domain.dailyoutfit.code;


import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ShoppingMallLink {
    MUSINSA("https://www.musinsa.com/search/musinsa/goods?q="),
    WCONCEPT("https://www.wconcept.co.kr/Search?query=");
    private final String template;

    ShoppingMallLink(String template) {
        this.template = template;
    }

    public String getUrl(String categoryName) {
        // 공백 → + 처리, 필요시 한글 URL encode도 가능
        String encoded = categoryName.replace(" ", "+");
        return String.format(template, encoded);
    }
    public static Map<String, String> getAllUrls(String categoryName) {
        return Arrays.stream(ShoppingMallLink.values())
                .collect(Collectors.toMap(
                        // Key: 쇼핑몰 이름 (MUSINSA, WCONCEPT)
                        Enum::name,
                        // Value: 생성된 검색 URL
                        mall -> mall.getUrl(categoryName)
                ));
    }
}