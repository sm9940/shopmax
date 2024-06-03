package com.shopmax.service;

import com.shopmax.dto.ItemFormDto;
import com.shopmax.dto.ItemImgDto;
import com.shopmax.dto.ItemSearchDto;
import com.shopmax.dto.MainItemDto;
import com.shopmax.entity.Item;
import com.shopmax.entity.ItemImg;
import com.shopmax.repository.ItemImgRepository;
import com.shopmax.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional //하나의 메소드가 트랜잭션으로 묶인다(DB Exception 혹은 다른 Exception 발생시 롤백)
@RequiredArgsConstructor
public class ItemService {
    private  final ItemRepository itemRepository;
    private  final ItemImgRepository itemImgRepository;
    private  final ItemImgService itemImgService;

    //item 테이블에 상품 등록(insert)
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        //1. 상품 등록(insert)
        Item item = itemFormDto.createItem(); //dto -> entity
        itemRepository.save(item);
        //2. 이미지 등록
        for (int i = 0; i<itemImgFileList.size(); i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item); //★itemImg가 item을 참조하므로 insert하기전 반드시 item 객체를 넣어준다.

            //첫번째 이미지 일때 대표 이미지 지정
            if(i == 0){
                itemImg.setRepImgYn("Y");
            } else {
                itemImg.setRepImgYn("N");
            }
            itemImgService.saveItemImg(itemImg,itemImgFileList.get(i));
        }
        return item.getId(); //등록한 상품의 아이디를 리턴
    }

    //상품 가져오기
    @Transactional(readOnly = true) //트랜잭션 읽기 전용(변경감지 수행 X) -> 성능향상
    public ItemFormDto getItemDtl(Long itemId){
        //1. item_img 테이블의 이미지를 가져온다.
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        //ItemImg Entity -> Dto 변환
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg: itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg); //dto객체로 바뀜
            itemImgDtoList.add(itemImgDto);//dto객체를 리스트에 넣어준다.
        }
        //2. item 테이블에 있는 데이터를 가져온다.
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        //Item Entity -> Dto 변환
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        //3.ItemFormDto에 itemImgdtoList를 넣어준다. -> 화면단에서는 ItemFormdto에서 이미지 리스트를 가지고옴
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        //1. item 엔티티 수정
        //update를 진행하기 전 무조건 select를 해온다.
        // -> 영속성 컨텍스트에 item 엔티티가 없다면 가져오기 위해
        Item item = itemRepository.findById(itemFormDto.getId()).orElseThrow(EntityNotFoundException::new);
        //update 실행
        //한번 select를 진행하면 엔티티가 영속성 컨텍스트에저장되고
        //변경 감지 기능으로 인해 트랜잭션 커밋 시점에 엔티티와 DB에 저장된값이
        //다른 내용은 JPA에서 update해준다
        item.updateItem(itemFormDto);

        //2. item_img 엔티티 수정

        List<Long> itemImgIds = itemFormDto.getItemImgIds(); //상품 이미지 아이디 리스트 조회

        //5개의 이미지 파일을 업로드 했으므로 아래처럼 for문을 이용해 하나씩 이미지 업데이트를 진해
        for (int i = 0; i < itemImgFileList.size(); i++) {
            //itemImgSerivce.updateItemImg(itemImg id, 이미지 파일);
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }
        return item.getId(); //변경한 item의 id리턴
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        Page<Item> itemPage =itemRepository.getAdminItemPage(itemSearchDto,pageable);
        return itemPage;
    }

    @Transactional(readOnly = true)
    public  Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto,Pageable pageable){
        Page<MainItemDto> mainItemPage = itemRepository.getMainItemPage(itemSearchDto,pageable);
        return mainItemPage;
    }
}
