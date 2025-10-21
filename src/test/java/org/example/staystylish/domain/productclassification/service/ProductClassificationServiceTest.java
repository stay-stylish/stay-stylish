package org.example.staystylish.domain.productclassification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.staystylish.domain.productclassification.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.productclassification.dto.response.ProductClassificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductClassificationServiceTest {

    private ProductClassificationService productClassificationService;

    @Mock
    private ChatModel mockChatModel;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ChatClient mockChatClient = ChatClient.builder(mockChatModel).build();
        productClassificationService = new ProductClassificationService(mockChatClient, objectMapper);
    }

    @Nested
    @DisplayName("classify 메서드는")
    class Describe_classify {

        private void setupMockChatModelResponse(String responseContent) {
            AssistantMessage assistantMessage = new AssistantMessage(responseContent);
            Generation mockGeneration = new Generation(assistantMessage);
            ChatResponse mockChatResponse = new ChatResponse(List.of(mockGeneration));
            when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        }

        @Nested
        @DisplayName("정상적인 AI 응답을 받으면")
        class Context_with_valid_ai_response {

            @Test
            @DisplayName("응답을 파싱하여 ProductClassificationResponse 객체를 반환한다")
            void it_returns_parsed_response() {
                // 준비
                ProductClassificationRequest requestDto = new ProductClassificationRequest("루즈핏 피그먼트 맨투맨");
                String aiResponseJson = "{\"category\":\"상의\",\"sub_category\":\"맨투맨\",\"style_tags\":[\"캐주얼\",\"스트릿\"]}";
                setupMockChatModelResponse(aiResponseJson);

                ProductClassificationResponse expectedResponse = new ProductClassificationResponse("상의", "맨투맨", List.of("캐주얼", "스트릿"));

                // 실행
                ProductClassificationResponse actualResponse = productClassificationService.classify(requestDto);

                // 검증
                assertThat(actualResponse).isEqualTo(expectedResponse);
            }
        }

        @Nested
        @DisplayName("AI 응답에 불필요한 텍스트가 포함되어도")
        class Context_with_extra_text_in_response {

            @Test
            @DisplayName("JSON 부분만 추출하여 성공적으로 파싱한다")
            void it_extracts_json_and_parses() {
                // 준비
                ProductClassificationRequest requestDto = new ProductClassificationRequest("쿨맥스 와이드 밴딩 슬랙스");
                String aiResponseWithExtraText = "물론이죠! 요청하신 상품에 대한 분석 결과입니다:\n\n{\"category\":\"하의\",\"sub_category\":\"슬랙스\",\"style_tags\":[\"미니멀\",\"포멀\"]}\n\n도움이 되셨기를 바랍니다.";
                setupMockChatModelResponse(aiResponseWithExtraText);

                ProductClassificationResponse expectedResponse = new ProductClassificationResponse("하의", "슬랙스", List.of("미니멀", "포멀"));

                // 실행
                ProductClassificationResponse actualResponse = productClassificationService.classify(requestDto);

                // 검증
                assertThat(actualResponse).isEqualTo(expectedResponse);
            }
        }

        @Nested
        @DisplayName("AI 응답이 잘못된 JSON 형식이면")
        class Context_with_malformed_json {

            @Test
            @DisplayName("RuntimeException을 발생시킨다")
            void it_throws_runtime_exception() {
                // 준비
                ProductClassificationRequest requestDto = new ProductClassificationRequest("잘못된 상품");
                String malformedJson = "{\"category\":\"상의\", \"sub_category\":\"티셔츠\""; // Malformed JSON
                setupMockChatModelResponse(malformedJson);

                // 실행 & 검증
                RuntimeException exception = assertThrows(RuntimeException.class, () -> productClassificationService.classify(requestDto));
                assertThat(exception.getMessage()).isEqualTo("AI 응답을 파싱하는 데 실패했습니다.");
            }
        }
    }
}
