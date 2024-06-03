package com.shopmax.dto;

import com.shopmax.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private String itemNm; //상품명
    private  int count; //주문수량
    private String imgUrl; //상품 이미지 경로
    private int orderPrice;
    //엔티티 -> Dto롤 바꿔준다.
    //OrderItem엔티티와 OrderItemDto는 속성이 일치하지 않으므로 modelMapper를 사용 X
    public OrderItemDto(OrderItem orderItem, String imgUrl) {
        this.itemNm =  orderItem.getItem().getItemNm();
        this.imgUrl = imgUrl;
        this.count = orderItem.getCount();
        this.orderPrice= orderItem.getOrderPrice();
    }
}

