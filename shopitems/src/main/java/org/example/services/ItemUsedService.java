package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.domains.*;
import org.example.domains.repositories.ItemUsedRepository;
import org.example.services.payloads.requests.ItemUsedRequest;
import org.example.services.payloads.responses.basicResponses.ItemUsedResponse;
import org.example.services.payloads.responses.dtos.ItemUsedDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ItemUsedService {

    @Inject
    ItemUsedRepository itemUsedRepository;

    private static final String NOT_FOUND = "Not found!";


    public ItemUsedDTO addToItemUsedToLabTest(ItemUsedRequest request) {
        Procedure labTest = Procedure.findById(request.labTestId);
        Item item = Item.findById(request.itemId);

        if (labTest == null || item == null) {
            throw new WebApplicationException("LabTest or Item not found", Response.Status.NOT_FOUND);
        }

        ItemUsed itemUsed = ItemUsed.find("labTest = ?1 and item = ?2", labTest, item).firstResult();

        if (itemUsed == null) {
            itemUsed = new ItemUsed();
            itemUsed.labTest = labTest;
            itemUsed.item = item;
            itemUsed.quantity = 1;
            itemUsed.total = item.sellingPrice.multiply(BigDecimal.valueOf(itemUsed.quantity)); // Assuming `unitPrice` exists
            itemUsed.persist();
        } else {
            itemUsed.quantity += 1;
            itemUsed.total = item.sellingPrice.multiply(BigDecimal.valueOf(itemUsed.quantity)); // Recalculate total
            itemUsed.persist();
        }

        // Return DTO
        return new ItemUsedDTO(itemUsed);
    }



    public List<ItemUsedDTO> getCart(Long labTestId) {
        // Fetch the list of ItemUsed entities by labTestId
        List<ItemUsed> itemUsedList = ItemUsed.find("labTest.id", labTestId).list();

        // Map the ItemUsed entities to ItemUsedDTOs using a method reference
        return itemUsedList.stream()
                .map(this::mapToDTO) // Replace lambda with method reference
                .collect(Collectors.toList()); // Collect the DTOs into a list
    }

    // Method to map ItemUsed entity to ItemUsedDTO
    private ItemUsedDTO mapToDTO(ItemUsed itemUsed) {
        return new ItemUsedDTO(itemUsed);
    }


    public ItemUsedResponse getAllCart(Long labTestId) {
        // Retrieve all shopping cart items for the user
        List<ItemUsed> cartItems = ItemUsed.find("labTest.id", labTestId).list();

        // Extract the ShopItem objects from the ShoppingCart entries
        List<Item> shopItems = cartItems.stream()
                .map(cartItem -> cartItem.item)
                .collect(Collectors.toList());

        // Calculate the total quantity by summing the quantity field of each cart item
        int totalQuantity = cartItems.stream()
                .mapToInt(cartItem -> cartItem.quantity)
                .sum();

        // Return the list of ShopItems and the total quantity as a CartResponse object
        return new ItemUsedResponse(shopItems, totalQuantity);
    }


}
