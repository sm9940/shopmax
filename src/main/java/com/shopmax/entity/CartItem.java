package com.shopmax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@ToString
public class CartItem extends BaseEntity{
    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int count; //상품수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id") //fk키
    private Cart cart; //CartItem은 Cart를 참조한다

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") //fk키
    private Item item; //CartItem은 item을 참조한다.
}
