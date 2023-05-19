package controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import logic.Cart;
import logic.Item;
import logic.ItemSet;
import logic.ShopService;

@Controller
@RequestMapping("cart")
public class CartController {
	@Autowired
	private ShopService service;
	
	@RequestMapping("cartAdd")
	public ModelAndView add(Integer id, Integer quantity,HttpSession session) {
		ModelAndView mav = new ModelAndView("cart/cart");
		Item item = service.getItem(id);
		Cart cart = (Cart)session.getAttribute("CART");
		if(cart == null) {
			cart = new Cart();
			session.setAttribute("CART", cart);
		}
		cart.push(new ItemSet(item,quantity));
		mav.addObject("message",item.getName()+":"+quantity + "개 장바구니 추가");
		mav.addObject("cart",cart);
		return mav;
	}
	@RequestMapping("cartDelete")
	public ModelAndView delete(int index,HttpSession session) {
		ModelAndView mav = new ModelAndView("cart/cart");
		Cart cart = (Cart)session.getAttribute("CART");
		ItemSet robj = cart.getItemSetList().remove(index);
		mav.addObject("message",robj.getItem().getName()+"가(이) 삭제되었습니다.");
		mav.addObject("cart",cart);
		return mav;
	}
	@RequestMapping("cartView")
	public ModelAndView view(HttpSession session) {
		ModelAndView mav = new ModelAndView("cart/cart");
		Cart cart = (Cart)session.getAttribute("CART");
		mav.addObject("cart",cart);
		return mav;
	}

	@RequestMapping("cartItemDelete")
	public ModelAndView delete2(int id,HttpSession session) {
		ModelAndView mav = new ModelAndView("cart/cart");
		Cart cart = (Cart)session.getAttribute("CART");
		ItemSet delItem = null;
		for (ItemSet is : cart.getItemSetList()) {
			if(is.getItem().getId() == id) {
				delItem = is; 
				cart.getItemSetList().remove(is);
				break;
			}
		}
		mav.addObject("message",delItem.getItem().getName()+"가(이) 삭제되었습니다.");
		mav.addObject("cart",cart);
		return mav;
	}
}
