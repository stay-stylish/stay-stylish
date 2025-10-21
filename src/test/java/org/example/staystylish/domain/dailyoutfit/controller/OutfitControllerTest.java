package org.example.staystylish.domain.dailyoutfit.controller;

import org.example.staystylish.domain.dailyoutfit.entity.UserItemFeedback;
import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;
import org.example.staystylish.domain.dailyoutfit.repository.UserItemFeedbackRepository;
import org.example.staystylish.domain.productclassification.entity.Product;
import org.example.staystylish.domain.productclassification.repository.ProductRepository;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OutfitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserItemFeedbackRepository userItemFeedbackRepository;

    private User testUser;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        userItemFeedbackRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // 컨트롤러는 userId = 1L을 사용하도록 하드코딩되어 있습니다. 테스트 DB가 ID 1을 할당하도록 사용자를 저장합니다.
        User userToSave = User.builder().email("test@example.com").password("password").nickname("tester").build();
        testUser = userRepository.save(userToSave);

        product1 = productRepository.save(Product.create("Test Shirt"));
        product2 = productRepository.save(Product.create("Test Pants"));
    }

    @Test
    @DisplayName("시나리오 1: 아이템 좋아요")
    void testLikeItem() throws Exception {
        // 실행
        mockMvc.perform(post("/api/v1/outfits/items/{itemId}/like", product1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("피드백이 성공적으로 저장되었습니다."));

        // 검증
        Optional<UserItemFeedback> feedback = userItemFeedbackRepository.findByUserIdAndProductId(testUser.getId(), product1.getId());
        assertThat(feedback).isPresent();
        assertThat(feedback.get().getLikeStatus()).isEqualTo(LikeStatus.LIKE);
    }

    @Test
    @DisplayName("시나리오 2: 아이템 좋아요 취소")
    void testUnlikeItem() throws Exception {
        // 준비
        UserItemFeedback initialFeedback = UserItemFeedback.create(testUser, product1, LikeStatus.LIKE);
        userItemFeedbackRepository.save(initialFeedback);

        // 실행
        mockMvc.perform(delete("/api/v1/outfits/items/{itemId}/like", product1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("피드백이 취소되었습니다."));

        // 검증
        Optional<UserItemFeedback> feedback = userItemFeedbackRepository.findByUserIdAndProductId(testUser.getId(), product1.getId());
        assertThat(feedback).isNotPresent();
    }

    @Test
    @DisplayName("시나리오 3: 아이템 싫어요")
    void testDislikeItem() throws Exception {
        // 실행
        mockMvc.perform(post("/api/v1/outfits/items/{itemId}/dislike", product2.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("피드백이 성공적으로 저장되었습니다."));

        // 검증
        Optional<UserItemFeedback> feedback = userItemFeedbackRepository.findByUserIdAndProductId(testUser.getId(), product2.getId());
        assertThat(feedback).isPresent();
        assertThat(feedback.get().getLikeStatus()).isEqualTo(LikeStatus.DISLIKE);
    }

    @Test
    @DisplayName("시나리오 4: 아이템 싫어요 취소")
    void testUndislikeItem() throws Exception {
        // 준비
        UserItemFeedback initialFeedback = UserItemFeedback.create(testUser, product2, LikeStatus.DISLIKE);
        userItemFeedbackRepository.save(initialFeedback);

        // 실행
        mockMvc.perform(delete("/api/v1/outfits/items/{itemId}/dislike", product2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("피드백이 취소되었습니다."));

        // 검증
        Optional<UserItemFeedback> feedback = userItemFeedbackRepository.findByUserIdAndProductId(testUser.getId(), product2.getId());
        assertThat(feedback).isNotPresent();
    }

    @Test
    @DisplayName("시나리오 5: 존재하지 않는 아이템에 피드백")
    void testFeedbackOnNonExistentItem() throws Exception {
        // 실행 & 검증
        long nonExistentItemId = 99999L;
        mockMvc.perform(post("/api/v1/outfits/items/{itemId}/like", nonExistentItemId))
                .andExpect(status().isNotFound());
    }
}
