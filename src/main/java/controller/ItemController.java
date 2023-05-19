package controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import logic.Item;
import logic.ShopService;

@Controller //@Component + Controller 기능
@RequestMapping("item")  //http://localhost:8080/shop1/item/*
public class ItemController {
	@Autowired   //ShopService 객체 주입. 
	private ShopService service;
	//http://localhost:8080/shop1/item/list
	@RequestMapping("list")
	public ModelAndView list() {
		//ModelAndView : Model + view 
		//               view에 전송할 데이터 + view 설정 
		// view 설정이 안된 경우 : url 과 동일. item/list 뷰로 설정 
		ModelAndView mav = new ModelAndView();
		//itemList : item 테이블의 모든 정보를 Item 객체 List로 저장
		List<Item> itemList = service.itemList();
		mav.addObject("itemList",itemList); //데이터 저장
                                    		//view : item/list 
		return mav;	
	}
	//http://localhost:8080/shop1/item/detail?id=1
	@RequestMapping("detail")
	public ModelAndView detail(Integer id) {
		//id = id 파라미터의 값.
		//매개변수 이름과 같은 이름의 파라미터값을 자동으로 저장함.
		System.out.println(id);
		ModelAndView mav = new ModelAndView();
		Item item = service.getItem(id);
		mav.addObject("item",item);
		return mav;	
	}
	@RequestMapping("create")
	public ModelAndView create() {
		ModelAndView mav = new ModelAndView();
		mav.addObject(new Item());
		return mav;
	}
	@RequestMapping("register")
	public ModelAndView register(@Valid Item item, BindingResult bresult) {
		//item의 프로퍼티와 파라미터값을 비교하여 같은 이름의 값을 item객체에 저장
		//@Valid :item 객체에 입력된 내용을 유효성 검사함 
		ModelAndView mav = new ModelAndView("item/create"); //view이름 설정
		if(bresult.hasErrors()) { //@Valid 프로세스에 의해서 입력 데이터 오류가 있는 경우
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		return mav;
	}
}
