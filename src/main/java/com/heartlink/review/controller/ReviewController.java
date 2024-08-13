package com.heartlink.review.controller;

import com.heartlink.review.common.Pagination;
import com.heartlink.review.model.dto.ReviewDto;
import com.heartlink.review.model.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final Pagination pagination;

    public ReviewController(ReviewService reviewService, Pagination pagination) {

        this.reviewService = reviewService;
        this.pagination = pagination;
    }

    @GetMapping("/photomain")
    public String photoMain(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 10;
        List<ReviewDto> reviews = reviewService.getAllReviews(); // 모든 리뷰를 가져옴

        Map<String, Object> paginationData = pagination.getPagination(page, pageSize, reviews);

        model.addAttribute("reviews", paginationData.get("items"));
        model.addAttribute("currentPage", paginationData.get("currentPage"));
        model.addAttribute("totalPages", paginationData.get("totalPages"));
        model.addAttribute("paginationUrl", "/review/photomain");

        return "/review/review_photo/photo-main";
    }

    @GetMapping("/photoenroll")
    public String photoEnroll(Model model) {
        // 임의의 userId로 유저 정보 가져오기 (로그인된 유저 정보를 가져와야 함)
        int userId = 9; // 이 값은 실제로는 로그인 세션이나 다른 방법을 통해 가져와야 합니다.
        String reviewerNickname = reviewService.getNicknameByUserId(userId);

        // ReviewDto 객체 생성 및 설정
        ReviewDto review = new ReviewDto();
        review.setReviewerUserId(userId);
        review.setReviewerNickname(reviewerNickname);

        // 모델에 review 객체 추가
        model.addAttribute("review", review);

        return "review/review_photo/photo-enroll";
    }


    @GetMapping("/photoedit")
    public String photoEdit(@RequestParam("reviewNo") int reviewNo, Model model) {
        ReviewDto review = reviewService.getReviewDetail(reviewNo);
        model.addAttribute("review", review);
        return "review/review_photo/photo-edit";
    }

    @GetMapping("/photodetail")
    public String photoDetail(@RequestParam("reviewNo") int reviewNo, Model model) {
        ReviewDto review = reviewService.getReviewDetail(reviewNo);
        model.addAttribute("review", review);
        return "/review/review_photo/photo-detail";
    }

    @GetMapping("/livemain")
    public String liveMain() {
        return "/review/live-main";
    }

    @PostMapping("/submit")
    public String submitPhotoEnroll(@RequestParam("title") String title,
                                    @RequestParam("content") String content,
                                    @RequestParam("userId") int userId, // Hidden 필드로 전달된 userId를 받음
                                    Model model) {
        try {
            ReviewDto review = new ReviewDto();
            review.setReviewTitle(title);
            review.setReviewContent(content);
            review.setReviewerUserId(userId); // Hidden 필드에서 가져온 userId를 설정

            boolean isSaved = reviewService.savePhotoReview(review, null);

            if (isSaved) {
                model.addAttribute("message", "글이 성공적으로 작성되었습니다.");
                return "redirect:/review/photomain";
            } else {
                model.addAttribute("message", "글 작성에 실패했습니다.");
                return "review/review_photo/photo-enroll";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "오류가 발생했습니다: " + e.getMessage());
            return "review/review_photo/photo-enroll";
        }
    }

    @PostMapping("/photoedit")
    public String updatePhotoReview(@RequestParam("reviewNo") int reviewNo,
                                    @RequestParam("reviewTitle") String title,
                                    @RequestParam("reviewContent") String content,
                                    Model model) {
        try {
            ReviewDto review = new ReviewDto();
            review.setReviewNo(reviewNo);
            review.setReviewTitle(title);
            review.setReviewContent(content);

            boolean isUpdated = reviewService.updatePhotoReview(review);

            if (isUpdated) {
                model.addAttribute("message", "글이 성공적으로 수정되었습니다.");
                return "redirect:/review/photodetail?reviewNo=" + reviewNo;
            } else {
                model.addAttribute("message", "글 수정에 실패했습니다.");
                return "review/review_photo/photo-edit";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "오류가 발생했습니다: " + e.getMessage());
            return "review/review_photo/photo-edit";
        }
    }

    @PostMapping("/delete")
    public String deleteReview(@RequestParam("reviewNo") int reviewNo, Model model) {
        try {
            boolean isDeleted = reviewService.deleteReview(reviewNo);

            if (isDeleted) {
                model.addAttribute("message", "리뷰가 성공적으로 삭제되었습니다.");
            } else {
                model.addAttribute("message", "리뷰 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/review/photomain";
    }

}
