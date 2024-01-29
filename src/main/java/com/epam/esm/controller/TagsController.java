package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.epam.esm.dto.TagRequestDTO;
import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.service.TagService;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class TagsController {

    @Autowired
    private TagService tagService;

    @Autowired
    private EntityManager entityManager;

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

    @GetMapping(value = "/most-used-tag")
    public ResponseEntity<EntityModel<TagResponseDTO>> getMostUsedTag(){
        TagResponseDTO tagResponseDTO = tagService.getMostUsedTag();
        EntityModel<TagResponseDTO> resource = EntityModel.of(tagResponseDTO);
        resource.add(linkTo(methodOn(TagsController.class)
            .getMostUsedTag()).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }

    @DeleteMapping(value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Void> deleteTagById(@PathVariable long id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity getTagRevisions(@PathVariable long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(Tag.class, true, true);
        query.addOrder(AuditEntity.revisionNumber().desc());
        List <Tag> resultList = new ArrayList<>();

        List <Number> revisionNumbers = reader.getRevisions(Tag.class, id);
        for (Number rev : revisionNumbers) {
            Tag auditedTag = reader.find(Tag.class, id, rev);
            resultList.add(auditedTag);
        }

        return ResponseEntity.ok(resultList);
    }
}
