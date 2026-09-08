package unaj.subastayaps.model;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    //@Column(nullable = false)
    //private String slug;

    public Categories() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    //public String getSlug() { return slug; }
    //public void setSlug(String slug) { this.slug = slug; }
}
