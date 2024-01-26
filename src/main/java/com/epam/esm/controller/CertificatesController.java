package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.epam.esm.dto.CertificateRequestDTO;
import com.epam.esm.dto.CertificateResponseDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.service.CertificateService;
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certificate")
public class CertificatesController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private EntityManager entityManager;

    @PostMapping(consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<EntityModel<CertificateResponseDTO>> postCertificate(@RequestBody CertificateRequestDTO requestDTO) {
        GiftCertificate giftCertificate = new GiftCertificate(
            requestDTO.name(),
            requestDTO.description(),
            requestDTO.price(),
            requestDTO.duration());

        CertificateResponseDTO returnDTO = certificateService.saveGiftCertificate(giftCertificate, requestDTO.tagIds());

        EntityModel<CertificateResponseDTO> resource = EntityModel.of(returnDTO);
        resource.add(linkTo(methodOn(CertificatesController.class)
            .getCertificate(returnDTO.certificateId())).withSelfRel());

        return ResponseEntity.status(CREATED).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CertificateResponseDTO>> getCertificate(@PathVariable long id) {
        CertificateResponseDTO certificateDTO = certificateService.getGiftCertificate(id);
        EntityModel<CertificateResponseDTO> resource = EntityModel.of(certificateDTO);
        resource.add(linkTo(methodOn(CertificatesController.class)
            .getCertificate(id)).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }

    //TODO integration test. Test here audit, pagination, HATEOAS (verify the links) Create testing data by hand

//    Post /certificates
//    PATCH /certificates/1
//    GET /certificates/1/revisions -> assert, that size of returned list is 2.
    @GetMapping("/{id}/revisions")
    public ResponseEntity getCertificateRevisions(@PathVariable long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(GiftCertificate.class, true, true);
        query.addOrder(AuditEntity.revisionNumber().desc());
        List <GiftCertificate> resultList = new ArrayList<>();

        List <Number> revisionNumbers = reader.getRevisions(GiftCertificate.class, id);
        for (Number rev : revisionNumbers) {
            GiftCertificate auditedCertificate = reader.find(GiftCertificate.class, id, rev);
            resultList.add(auditedCertificate);
        }

        return ResponseEntity.ok(resultList);
    }
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<CertificateResponseDTO>>> getFilteredCertificates(
        @RequestParam(required = false) List<String> tagName,
        @RequestParam(required = false) String searchWord,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sort,
        PagedResourcesAssembler<CertificateResponseDTO> assembler) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CertificateResponseDTO> certificatesPage =
            certificateService.getFilteredCertificates(tagName, searchWord, pageable);

        Link selfLink = linkTo(methodOn(CertificatesController.class).getFilteredCertificates(tagName,
            searchWord, page, size, sort, assembler)).withSelfRel();

        return ResponseEntity.ok(assembler.toModel(certificatesPage,
            cert -> EntityModel.of(cert,
                linkTo(CertificatesController.class).slash(cert.certificateId()).withSelfRel()), selfLink));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable long id) {
        certificateService.deleteGiftCertificate(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EntityModel<CertificateResponseDTO>> updateCertificate(@PathVariable Long id,
        @RequestBody CertificateRequestDTO requestDTO){
        CertificateResponseDTO returnCertificate = certificateService.updateGiftCertificate(id,
            new GiftCertificate(requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.duration()),
            requestDTO.tagIds());

        EntityModel<CertificateResponseDTO> resource = EntityModel.of(returnCertificate);
        resource.add(linkTo(methodOn(CertificatesController.class).getCertificate(id)).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }
}
