package unaj.subastaya.controller;

import org.springframework.web.bind.annotation.*;
import unaj.subastaya.repository.CategoriesRepository;
import unaj.subastaya.dto.CategoryDto;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoriesRepository categoriesRepository;

    public CategoryController(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }
    @GetMapping
    public List<CategoryDto> getCategories() {
        return categoriesRepository.findAll()
                .stream()
                .map(category -> new CategoryDto(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }
}