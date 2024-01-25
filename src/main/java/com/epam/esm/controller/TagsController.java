package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.epam.esm.dto.TagRequestDTO;
import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
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
    public ResponseEntity<EntityModel<TagResponseDTO>> postTag(@RequestBody TagRequestDTO requestDTO) {
        TagResponseDTO tagDTO = tagService.saveTag(requestDTO.name());

        EntityModel<TagResponseDTO> resource = EntityModel.of(tagDTO);
        resource.add(linkTo(TagsController.class).slash(tagDTO.id()).withSelfRel());

        return ResponseEntity.status(CREATED).body(resource);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TagResponseDTO>>> getAllTags(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sort,
        PagedResourcesAssembler<TagResponseDTO> assembler) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        Page<TagResponseDTO> tagDTOPage = tagService.getAllTags(pageable);

        PagedModel<EntityModel<TagResponseDTO>> pagedModel = assembler.toModel(tagDTOPage,
            tagDTO -> EntityModel.of(tagDTO,
                linkTo(TagsController.class).slash(tagDTO.id()).withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping(value = "/popular")
    public ResponseEntity<EntityModel<TagResponseDTO>> getMostPopularTag(){
        TagResponseDTO tagResponseDTO = tagService.getMostPopularTag();
        EntityModel<TagResponseDTO> resource = EntityModel.of(tagResponseDTO);
        resource.add(linkTo(methodOn(TagsController.class)
            .getMostPopularTag()).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }

    @DeleteMapping(value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Void> deleteTagById(@PathVariable long id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
