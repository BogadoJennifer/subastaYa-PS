package unaj.subastayaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unaj.subastayaps.model.Categories;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Long> {
}