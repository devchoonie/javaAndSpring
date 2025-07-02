package kr.choonie.shop.service;

import jakarta.persistence.EntityManager;
import kr.choonie.shop.dto.PageDto;
import kr.choonie.shop.dto.ReviewCounterDto;
import kr.choonie.shop.dto.ReviewDto;
import kr.choonie.shop.entity.*;
import kr.choonie.shop.make.PageInfo;
import kr.choonie.shop.mapper.ReviewMapper;
import kr.choonie.shop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final EntityManager entityManager;
    private final ReviewMapper reviewMapper;


    @Transactional(readOnly = true)
    public List<ReviewCounterDto> selectWritableList(String mId, int productId) {

        return reviewRepository.selectWritableList(mId, productId);

    }


    //리뷰 삭제 시 활성 플래그 변경
    @Transactional(rollbackFor = Exception.class)
    public void updateReviewIsDeleted(ReviewDto reviewDto) {

        Review review = reviewRepository.findReview(reviewDto.getReviewId(), reviewDto.getMId());
        if (review == null) {
            throw new NullPointerException();
        }

        if (review.isDeleted() != reviewDto.isDeleted()) {
            review.setDeleted(reviewDto.isDeleted());
        }
    }

    //리뷰 목록에서 내 리뷰를 수정할 때
    @Transactional(rollbackFor = Exception.class)
    public ReviewDto updateReview(ReviewDto reviewDto) {

        Review review = reviewRepository.findReview(reviewDto.getReviewId(), reviewDto.getMId());
        if (review == null) {
            throw new NullPointerException();
        }

        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        return ReviewDto.toDtoWithoutOtherDto(review);
    }


    @Transactional(rollbackFor = Exception.class)
    public Review save(ReviewDto dto) {

        String mId = dto.getMId();
        Long orderItemId = dto.getOrderItemId();
        String content = dto.getContent();
        int rating = dto.getRating();

        Member memberProxy = entityManager.getReference(Member.class, mId);
        OrderItem orderItemProxy = entityManager.getReference(OrderItem.class, orderItemId);

        Review review = Review.builder()
                .member(memberProxy)
                .orderItem(orderItemProxy)
                .content(content)
                .rating(rating)
                .build();


        return reviewRepository.save(review);

    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }


    @Transactional(readOnly = true)
    public Map<String, Object> selectReviews(Map<String, Object> map) {

        int totalRecord = reviewMapper.selectReviewsCount(map);
        int requestPage = 1;
        if (map.get("requestPage") != null) {
            requestPage = Integer.parseInt(String.valueOf(map.get("requestPage")));
        }


        int rowCount = 3;
        PageDto pageDto = PageInfo.getPageDto(requestPage, totalRecord, rowCount);
        pageDto.setListCount(rowCount);


        map.put("startRecord", pageDto.getStartRecord());
        map.put("rowCount", pageDto.getListCount());
        List<ReviewDto> reviewDtos = reviewMapper.selectReviews(map);


        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("pageDto", pageDto);
        returnMap.put("reviewDtos", reviewDtos);

        return returnMap;
    }


    @Transactional(readOnly = true)
    public ReviewDto selectReview(Map<String, Object> map) {
        return reviewMapper.selectReview(map);
    }
}
