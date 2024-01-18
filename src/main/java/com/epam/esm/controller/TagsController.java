package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.epam.esm.dto.TagRequestDTO;
import com.epam.esm.dto.TagReturnDTO;
import com.epam.esm.service.TagService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
public class TagsController {

    @Autowired
    private TagService tagService;

    @PostMapping(consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<EntityModel<TagReturnDTO>> postTag(@RequestBody TagRequestDTO requestDTO) {
        TagReturnDTO tagDTO = tagService.saveTag(requestDTO.name());

        EntityModel<TagReturnDTO> resource = EntityModel.of(tagDTO);
        resource.add(linkTo(TagsController.class).slash(tagDTO.id()).withSelfRel());

        return ResponseEntity.status(CREATED).body(resource);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TagReturnDTO>>> getAllTags(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<TagReturnDTO> tagDTOPage = tagService.getAllTags(pageable);

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            tagDTOPage.getSize(),
            tagDTOPage.getNumber(),
            tagDTOPage.getTotalElements(),
            tagDTOPage.getTotalPages());

        List<EntityModel<TagReturnDTO>> tagResources = tagDTOPage.getContent().stream()
            .map(tagDTO -> EntityModel.of(tagDTO,
                linkTo(TagsController.class).slash(tagDTO.id()).withSelfRel()))
            .collect(Collectors.toList());

        PagedModel<EntityModel<TagReturnDTO>> pagedModel = PagedModel.of(tagResources, pageMetadata);

        return ResponseEntity.ok(pagedModel);
    }

    @DeleteMapping(value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Void> deleteTagById(@PathVariable long id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
