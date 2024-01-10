package com.epam.esm.controller;

import com.epam.esm.Dto.Tag.TagRequestDTO;
import com.epam.esm.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TagsController {

    @Autowired
    private TagService tagService;

    /**
     * Creates a new tag.
     *
     * @param requestDTO The data transfer object containing the details of the tag to be created.
     * @return ResponseEntity<?> A response entity representing the result of the creation operation.
     *      It could contain the created tag, a custom message, or nothing (in case of void).
     *      If tag name is not valid, returns bad request
     *      if tag already exists, returns bad request
     *      if tag does not exist, but cannot be saved, return bad request
     *      if it is saved, return CREATED, and tag saved
     * @PostMapping This annotation maps HTTP POST requests onto this method.
     * @value "/tag" The path where this method is mapped.
     * @consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * @produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @PostMapping(value = "/tag", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> postTag(@RequestBody TagRequestDTO requestDTO) {
        return tagService.saveTag(requestDTO.name());
    }

    /**
     * Retrieves a tag by its ID.
     *
     * @param id The unique identifier of the tag to be retrieved.
     * @return ResponseEntity<?> A response entity containing the tag with the given ID.
     * can return the requested tag or not found
     * @GetMapping This annotation maps HTTP GET requests onto this method.
     * @value "/tag/{id}" The path where this method is mapped. It includes a path variable 'id'.
     * @consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * @produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @GetMapping(value = "/tag/{id}", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> getTagById(@PathVariable long id) {
        return tagService.getTag(id);
    }

    /**
     * Deletes a tag by its ID.
     *
     * @param id The unique identifier of the tag to be deleted.
     * @return ResponseEntity<?> A response entity representing the result of the deletion operation.
     * can return found or not found
     * @DeleteMapping This annotation maps HTTP DELETE requests onto this method.
     * @value "/tag/{id}" The path where this method is mapped. It includes a path variable 'id'.
     * @consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * @produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @DeleteMapping(value = "/tag/{id}", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> deleteTagById(@PathVariable long id) {
        return tagService.deleteTag(id);
    }
}
