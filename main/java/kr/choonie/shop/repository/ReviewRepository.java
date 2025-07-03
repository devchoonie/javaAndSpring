package kr.choonie.shop.repository;

import kr.choonie.shop.dto.ReviewCounterDto;
import kr.choonie.shop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {


    @Query(value = "select \n" +
            " \toi.order_code as orderCode ,\n" +
            "    oit.order_item_id as orderItemId,\n" +
            "    date_format(oi.order_at, '%y.%m.%d 주문') as orderAt,\n" +
            "    concat(c.color_name_en, '/', s.size_name) as optionName\n" +
            "from\n" +
            "    order_item oit\n" +
            "inner join\n" +
            "    order_info oi on oit.order_code = oi.order_code\n" +
            "inner join\n" +
            "    product_item pi on oit.product_item_id = pi.product_item_id\n" +
            "inner join\n" +
            "    product p on pi.product_id = p.product_id\n" +
            "inner join\n" +
            "    color c on pi.color_id = c.color_id\n" +
            "inner join\n" +
            "    size s on pi.size_id = s.size_id\n" +
            "left outer join\n" +
            "    review r on oit.order_item_id = r.order_item_id\n" +
            "where\n" +
            "\t1=1 and\n" +
            "    oi.m_id = :mId \n" +
            "    and pi.product_id = :productId \n" +
            "    and oi.order_status_id = 6 \n" +
            "    and r.review_id is null\n" +
            "    and exists (\n" +
            "        select 1\n" +
            "        from order_delivery od\n" +
            "        where od.order_code = oi.order_code\n" +
            "          and NOW() between od.done_at and DATE_ADD(od.done_at, interval 7 day)\n" +
            "    )", nativeQuery = true)
    List<ReviewCounterDto> selectWritableList(@Param("mId") String mId, @Param("productId") int productId);

    @Query(value = "select review_id from review where order_code = :orderCode and m_id = :mId and product_id = :productId",nativeQuery = true)
    Long findReviewId(@Param("orderCode") String orderCode,  @Param("mId") String mId, @Param("productId") int productId);

    @Query(value = "select * from review where review_id = :reviewId and m_id = :mId", nativeQuery = true)
    Review findReview(@Param("reviewId")Long reviewId,@Param("mId") String mId);
}
