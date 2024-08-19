package com.tenco.bank.board;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tenco.bank.repository.model.Blog;
import com.tenco.bank.service.BlogService;

@Controller
public class BoardController {
	private final BlogService blogService;

	public BoardController(BlogService blogService) {
		this.blogService = blogService;
	}

	@GetMapping("/board/index")
	public String index(Model model, @RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "5") int size) {
		
		int totalRecords = blogService.selectCount();
		int totalPages = (int) Math.ceil( (double)totalRecords / size );
		
		List<Blog> blogList = blogService.select(page, size);
		
		model.addAttribute("currentPage", page);
		model.addAttribute("blogList", blogList);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("size", size);
		
		return "/board/index";
	}

	@GetMapping("/board/saveForm")
	public String saveForm() {
		return "board/saveForm";
	}

	@GetMapping("/board/updateForm")
	public String updateForm(@RequestParam(name = "id", required = false) int id,Model model) {
		Blog blog = blogService.selectById(id);
		model.addAttribute("blog",blog);
		return "board/updateForm";
	}

	@PostMapping("/board/save")
	public String save(Blog blog) {
		blogService.create(blog);
		return "redirect:/board/index";
	}

	@PostMapping("/board/update")
	public String update(Blog blog,@RequestParam(name = "id", required = false) int id) {
		
		blogService.update(blog);
		return "redirect:/board/index";
	}

	@PostMapping("/board/delete")
	public String delete(@RequestParam(name = "id", required = false) int id) {
		blogService.delete(id);
		return "redirect:/board/index";
	}
}
