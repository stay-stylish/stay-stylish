package org.example.staystylish.domain.dailyoutfit.enums;


import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 쇼핑몰 링크 정보를 상수화하여 정의하는 열거형(Enum) 클래스.
 */
public enum ShoppingMallLink {
    MUSINSA("https://www.musinsa.com/search/musinsa/goods?q="),
    WCONCEPT("https://www.wconcept.co.kr/Search?query=");
    private final String template;

    ShoppingMallLink(String template) { // 각 상수가 정의될 때 호출되어 기본 URL을 `template` 필드에 할당.
        this.template = template;
    }

    //특정 '카테고리 이름'을 받아 해당 쇼핑몰의 '완성된 검색 URL'을 반환하는 인스턴스 메서드.
    public String getUrl(String categoryName) {
        String encoded = categoryName.replace(" ", "+");
        // "빈칸 채우기" 규칙 (템플릿이라는 문장에서 빈칸을 찾아서, 값으로 채워 넣어줘.)
        // 실제 String.format이 동작하려면 template 문자열에 `%s`와 같은 포맷 지정자가 있어야 함.
        // 예) template이 "https://...&q=%s"여야 인코딩된 문자열이 붙음.

        return String.format(template, encoded);
        //기본 템플릿(`template`)에 치환된 카테고리 이름(`encoded`)을 삽입하여 최종 URL을 생성 및 반환.
    }
    public static Map<String, String> getAllUrls(String categoryName) {
        return Arrays.stream(ShoppingMallLink.values())
                //`ShoppingMallLink` enum의 모든 상수(MUSINSA, WCONCEPT)를 배열로 가져와 스트림으로 변환.
                .collect(Collectors.toMap(
                        // 스트림의 각 요소를 Key-Value 쌍의 Map으로 수집하는 작업을 수행.
                        // Key: 쇼핑몰 이름 (MUSINSA, WCONCEPT)
                        Enum::name,
                        // Map의 Key를 Enum 상수의 이름(문자열)으로 지정.
                        // Value: 생성된 검색 URL
                        mall -> mall.getUrl(categoryName)
                        // Map의 Value를 생성. 각 쇼핑몰 상수에 대해 `getUrl` 메서드를 호출하여 완성된 URL을 값으로 지정.
                ));
    }
}