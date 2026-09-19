package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.CategoriesRepository;
import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.dto.CreateAuctionRequestDto;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Categories;
import unaj.subastaya.model.User;
import unaj.subastaya.exception.ResourceNotFoundException;

import java.time.LocalDateTime;

@Service
public class AuctionPublicationService {

    private final AuctionRepository auctionRepository;
    private final CategoriesRepository categoriesRepository;
    private final UserRepository userRepository;

    public AuctionPublicationService(
            AuctionRepository auctionRepository,
            CategoriesRepository categoriesRepository,
            UserRepository userRepository) {

        this.auctionRepository = auctionRepository;
        this.categoriesRepository = categoriesRepository;
        this.userRepository = userRepository;
    }
    public Auction createAuction(CreateAuctionRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        if (!request.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException(
                    "La fecha de finalización debe ser posterior a la fecha de inicio"
            );
        }
        if (!request.getEndDate().isAfter(now)) {
            throw new IllegalArgumentException(
                    "La fecha de finalización debe ser posterior a la fecha actual"
            );
        }
        Categories category = categoriesRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La categoría indicada no existe"
                ));
        User vendor = userRepository.findByEmail("vendor@test.com")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El vendedor no existe"
                ));

        Auction auction = new Auction();
        auction.setTitle(request.getTitle());
        auction.setDescription(request.getDescription());
        auction.setImageUrl(request.getImageUrl());
        auction.setBasePrice(request.getBasePrice());
        auction.setMinimumIncrement(request.getMinimumIncrement());
        auction.setStartDate(request.getStartDate());
        auction.setEndDate(request.getEndDate());
        auction.setCategories(category);
        auction.setVendor(vendor);

        if (request.getStartDate().isAfter(LocalDateTime.now())) {
            auction.setState("SCHEDULED");
        } else {
            auction.setState("ACTIVE");
        }
        return auctionRepository.save(auction);

    }
}
