package kr.choonie.shop.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.choonie.shop.dto.ReviewCounterDto;
import kr.choonie.shop.dto.ReviewDto;
import kr.choonie.shop.make.ValidAccount;
import kr.choonie.shop.mapper.ReviewMapper;
import kr.choonie.shop.service.ImageService;
import kr.choonie.shop.service.ReviewProcessService;
import kr.choonie.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReviewController {


    private final ReviewService reviewService;
    private final ReviewProcessService reviewProcessService;
    private final ValidAccount validAccount;

    @ResponseBody
    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> saveReview(
            @RequestParam(value = "productId", required = true) int productId,
            @RequestParam(value = "content", required = true) String content,
            @RequestParam(value = "orderItemId", required = true) String orderItemId,
            @RequestParam(value = "rating", required = true) int rating,
            @RequestParam(value = "images", required = false) MultipartFile[] files
    ) {


        if (!validAccount.isLogin()) {
            return new ResponseEntity<>(Map.of("result", false, "message", "로그인 하셔야 합니다."), HttpStatus.UNAUTHORIZED);
        }

        String mId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<ReviewCounterDto> reviewCounterDtos = reviewService.selectWritableList(mId, productId);
        if (reviewCounterDtos == null || reviewCounterDtos.isEmpty()) {
            return new ResponseEntity<>(Map.of("result", false, "message", "리뷰를 남길 주문상품이 조회되지 않았습니다."), HttpStatus.UNAUTHORIZED);
        }
        Long longOrderItemId = Long.parseLong(orderItemId);

        long count = reviewCounterDtos.stream().filter(dto -> longOrderItemId.equals(dto.getOrderItemId())).count();

        if (count <= 0) {
            return new ResponseEntity<>(Map.of("result", false, "message", "작성할 수 없습니다."), HttpStatus.UNAUTHORIZED);
        }


        ReviewDto reviewDto = ReviewDto.builder()
                .mId(mId)
                .orderItemId(longOrderItemId)
                .content(content)
                .rating(rating)
                .build();

        reviewProcessService.saveReview(reviewDto, files);


        return new ResponseEntity<>(Map.of("result", true, "reviewCountValue", reviewCounterDtos.size()-1), HttpStatus.OK);

    }

    //리뷰 목록 페이징
    @ResponseBody
    @PostMapping("/review/api/list")
    public ResponseEntity<Map<String, Object>> reviewList(
            @RequestParam(value = "productId", required = true) int productId,
            @RequestParam(value = "requestPage", required = false, defaultValue = "1") int requestPage
            ) {

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("requestPage", requestPage);
        Map<String, Object> returnMap = reviewService.selectReviews(map);

        return new ResponseEntity<>(returnMap, HttpStatus.OK);
    }

    //리뷰 삭제
    @ResponseBody
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable("reviewId") Long reviewId) {

      Map<String, Object> map = reviewProcessService.delete(reviewId);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    //리뷰 한건 보기
    @ResponseBody
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> getReview(
            @PathVariable(value = "reviewId", required = true) Long reviewId
    ){

        if(!validAccount.isLogin()){
            return new ResponseEntity<>(Map.of("result", false), HttpStatus.UNAUTHORIZED);
        }
        if(reviewId == null){
            return new ResponseEntity<>(Map.of("result", false), HttpStatus.BAD_REQUEST);
        }

        String mId = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("mId", mId);
        map.put("reviewId", reviewId);
        ReviewDto dto = reviewService.selectReview(map);

        if(dto == null){
            return new ResponseEntity<>(Map.of("result", false), HttpStatus.BAD_REQUEST);
        }

        System.out.println(dto);
        map.clear();
        map.put("reviewDto", dto);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    @ResponseBody
    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @ModelAttribute ReviewDto reviewDto,
            @RequestParam(value = "images", required = false) MultipartFile[] files
    ){

        if(!validAccount.isLogin()){
            return new ResponseEntity<>(Map.of("result", false), HttpStatus.UNAUTHORIZED);
        }
        String mId = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewDto.setMId(mId);

        Map<String, Object> map = reviewProcessService.updateReview(reviewDto, files);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }




}



