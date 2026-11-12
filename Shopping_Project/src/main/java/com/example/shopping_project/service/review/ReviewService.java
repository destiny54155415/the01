package com.example.shopping_project.service.review;

import com.example.shopping_project.dto.review.ReviewCreateRequestDTO;
import com.example.shopping_project.dto.review.ReviewResponseDTO;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.order.OrderItem;
import com.example.shopping_project.entity.order.OrderStatus;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.entity.order.Review;
import com.example.shopping_project.repository.order.OrderItemRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import com.example.shopping_project.repository.review.ReviewRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderItemRepository orderItemRepository,
                         ProductRepository productRepository,
                         EntityManager entityManager) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    // 리뷰 작성
    public ReviewResponseDTO createReview(Long memberId,
                                          ReviewCreateRequestDTO request) {

        // 별점 검사
        validateRating(request.getRating());

        // 리뷰 내용 검사
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // 회원 조회
        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        // 주문 상품 조회
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() ->
                        new IllegalArgumentException("주문 상품을 찾을 수 없습니다."));

        // 주문한 회원인지 확인
        if (!orderItem.getOrder().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 주문한 상품만 리뷰를 작성할 수 있습니다.");
        }

        // 배송 완료 여부 확인
        if (orderItem.getOrder().getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("배송 완료된 상품만 리뷰를 작성할 수 있습니다.");
        }

        // 구매한 상품 확인
        ProductEntity product = orderItem.getProduct();

        if (product == null) {
            throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다.");
        }

        // 중복 리뷰 확인
        if (reviewRepository.existsByMemberAndProduct(member, product)) {
            throw new IllegalArgumentException("이미 해당 상품의 리뷰를 작성했습니다.");
        }

        // 리뷰 생성 및 저장
        Review review = new Review(
                member,
                product,
                orderItem,
                request.getRating(),
                request.getContent()
        );

        Review savedReview = reviewRepository.save(review);

        return convertToDTO(savedReview);
    }

    // 상품별 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewList(Long productId) {

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다."));

        List<Review> reviews =
                reviewRepository.findByProductOrderByCreatedAtDesc(product);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 내가 작성한 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviewList(Long memberId) {

        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        List<Review> reviews = reviewRepository.findByMember(member);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // 내가 작성한 특정 리뷰 조회
    @Transactional(readOnly = true)
    public ReviewResponseDTO getMyReview(Long reviewId, Long memberId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 본인이 작성한 리뷰인지 확인
        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException(
                    "본인이 작성한 리뷰만 조회할 수 있습니다."
            );
        }

        return convertToDTO(review);
    }


    // 관리자 리뷰 전체 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviewList() {

        List<Review> reviews =
                reviewRepository.findAllByOrderByCreatedAtDesc();

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // 관리자 리뷰 검색
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> searchReviews(String keyword) {

        List<Review> reviews =
                reviewRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(keyword);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    public ReviewResponseDTO updateReview(Long reviewId,
                                          Long memberId,
                                          Integer rating,
                                          String content) {

        validateRating(rating);

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 본인이 작성한 리뷰인지 확인
        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(rating, content);

        return convertToDTO(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, Long memberId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 본인이 작성한 리뷰인지 확인
        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }
    // 관리자 리뷰 삭제
    public void deleteReviewByAdmin(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        reviewRepository.delete(review);
    }

    // 상품 평균 별점 조회
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Double average =
                reviewRepository.findAverageRatingByProduct(product);

        return average == null ? 0.0 : average;
    }

    // 상품 리뷰 개수 조회
    @Transactional(readOnly = true)
    public long getReviewCount(Long productId) {

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return reviewRepository.countByProduct(product);
    }

    // 별점 유효성 검사
    private void validateRating(Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1점부터 5점까지 입력할 수 있습니다.");
        }
    }

    // Entity를 DTO로 변환
    private ReviewResponseDTO convertToDTO(Review review) {

        return new ReviewResponseDTO(
                review.getReviewId(),
                review.getMember().getName(),
                review.getProduct().getProductName(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public boolean hasReview(Long memberId, Long productId) {

        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return reviewRepository.existsByMemberAndProduct(member, product);
    }
}