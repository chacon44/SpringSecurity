package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import com.epam.esm.dto.TagRequestDTO;
import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.TagService;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.AuditReader;
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
@RequestMapping("/api")
public class TagsController {

    private final String TAG = "/tag";
    private final String ID = "/{id}";
    private final String REVISIONS = "/revisions";

    @Autowired
    private TagService tagService;

    @Autowired
    private AuditReaderService auditReaderService;

    public TagsController(TagService tagService, AuditReaderService auditReaderService) {
        this.tagService = tagService;
        this.auditReaderService = auditReaderService;
    }

    /**
     * Saves a new Tag.
     *
     * @param requestDTO The data of the new Tag to create.
     * @return A ResponseEntity containing the saved Tag as a TagResponseDTO.
     */
    @PostMapping(value = TAG, consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<EntityModel<TagResponseDTO>> postTag(@RequestBody TagRequestDTO requestDTO) {
        TagResponseDTO tagDTO = tagService.saveTag(requestDTO.name());

        EntityModel<TagResponseDTO> resource = EntityModel.of(tagDTO);
        resource.add(linkTo(TagsController.class).slash(tagDTO.id()).withSelfRel());

        return status(CREATED).body(resource);
    }

    /**
     * Fetches all Tags according to the given paging and sorting parameters.
     *
     * @param page The number of the page to retrieve.
     * @param size The size of the pages.
     * @param sort The property by which to sort the results.
     * @param assembler Helps convert the Page into a PagedModel.
     * @return A ResponseEntity containing a PagedModel with all Tags.
     */
    @GetMapping(TAG)
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

    /**
     * Fetches the most used tag.
     *
     * @return A ResponseEntity containing the most used Tag as a TagResponseDTO.
     */
    @GetMapping(value = TAG + "/most-used-tag")
    public ResponseEntity<EntityModel<TagResponseDTO>> getMostUsedTag(){
        TagResponseDTO tagResponseDTO = tagService.getMostUsedTag();
        EntityModel<TagResponseDTO> resource = EntityModel.of(tagResponseDTO);
        resource.add(linkTo(methodOn(TagsController.class)
            .getMostUsedTag()).withSelfRel());

        return status(OK).body(resource);
    }

    /**
     * Deletes a Tag by id.
     *
     * @param id The id of the Tag to delete.
     * @return A ResponseEntity with the status code.
     */
    @DeleteMapping(TAG + ID)
    public ResponseEntity<Void> deleteTagById(@PathVariable long id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    /**
     * Fetches a tag by its ID.
     *
     * @param id The id of the Tag to be retrieved.
     * @return A ResponseEntity containing the TagResponseDTO.
     */
    @GetMapping(TAG + ID)
    public ResponseEntity<EntityModel<TagResponseDTO>> getTag(@PathVariable Long id) {
        TagResponseDTO tagResponseDTO = tagService.getTag(id);
        EntityModel<TagResponseDTO> resource = EntityModel.of(tagResponseDTO);
        resource.add(linkTo(methodOn(TagsController.class).getTag(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    /**
     * Fetches all revisions of a Tag by id.
     *
     * @param id The id of the Tag for which to fetch revisions.
     * @return A ResponseEntity containing all revisions of the Tag as a List.
     */
    @GetMapping(TAG + ID + REVISIONS)
    public ResponseEntity<?> getTagRevisions(@PathVariable long id) {
        AuditReader reader = auditReaderService.getReader();
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
