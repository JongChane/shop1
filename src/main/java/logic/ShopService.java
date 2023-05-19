package logic;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dao.ItemDao;
@Service   //@Component + Service(controller 기능과 dao 기능의 중간 역할 기능) 
public class ShopService {
	@Autowired //객체 주입
	private ItemDao itemDao;

	public List<Item> itemList() {
		return itemDao.list();
	}
	public Item getItem(Integer id) {
		return itemDao.getItem(id);
	}
	public void itemCreate(Item item, HttpServletRequest request) {
		if(item.getPicture() != null && !item.getPicture().isEmpty()) {
			//업로드해야하는 파일의 내용이 있는 경우
			String path = request.getServletContext().getRealPath("/")+ "img/";
			uploadFileCreate(item.getPicture(),path);
			//업로드된 파일이름
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}
		//db에 내용 저장
		int maxid = itemDao.maxId(); //item 테이블에 저장된 최대 id값
		item.setId(maxid+1);
		itemDao.insert(item); //db에 데이터 추가 
	}
	private void uploadFileCreate(MultipartFile file, String path) {
		//file : 파일의 내용
		//path : 업로드할 폴더
		String orgFile = file.getOriginalFilename(); //파일이름
		File f = new File(path);
		if(!f.exists()) f.mkdirs();
		try {
			//transferTo : file에 저장된 내용을 파일로 저장
			file.transferTo(new File(path+orgFile));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void itemUpdate(Item item, HttpServletRequest request) {
		if(item.getPicture() != null && !item.getPicture().isEmpty()) {
			String path = request.getServletContext().getRealPath("/")+ "img/";
			uploadFileCreate(item.getPicture(),path);
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}
		itemDao.update(item); 
	}
	public void itemDelete(Integer id) {
		itemDao.delete(id);
	}
}
