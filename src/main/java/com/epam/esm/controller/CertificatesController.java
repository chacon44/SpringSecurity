package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.epam.esm.dto.CertificateRequestDTO;
import com.epam.esm.dto.CertificateReturnDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.service.CertificateService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
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

    @PostMapping(consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<EntityModel<CertificateReturnDTO>> postCertificate(@RequestBody CertificateRequestDTO requestDTO) {
        GiftCertificate giftCertificate = new GiftCertificate(
            requestDTO.name(),
            requestDTO.description(),
            requestDTO.price(),
            requestDTO.duration());

        CertificateReturnDTO returnDTO = certificateService.saveGiftCertificate(giftCertificate, requestDTO.tagIds());

        EntityModel<CertificateReturnDTO> resource = EntityModel.of(returnDTO);
        resource.add(linkTo(methodOn(CertificatesController.class)
            .getCertificate(returnDTO.certificateId())).withSelfRel());

        return ResponseEntity.status(CREATED).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CertificateReturnDTO>> getCertificate(@PathVariable long id) {
        CertificateReturnDTO certificateDTO = certificateService.getGiftCertificate(id);
        EntityModel<CertificateReturnDTO> resource = EntityModel.of(certificateDTO);
        resource.add(linkTo(methodOn(CertificatesController.class)
            .getCertificate(id)).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<CertificateReturnDTO>>> getFilteredCertificates(
        @RequestParam(required = false) List<String> tagName,
        @RequestParam(required = false) String searchWord,
        Pageable pageable,
        PagedResourcesAssembler<CertificateReturnDTO> assembler) {

        Page<CertificateReturnDTO> certificatesPage =
            certificateService.getFilteredCertificates(tagName, searchWord, pageable);

        return ResponseEntity.ok(assembler.toModel(certificatesPage,
            cert -> EntityModel.of(cert,
                linkTo(methodOn(CertificatesController.class).getCertificate(cert.certificateId())).withSelfRel())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable long id) {
        certificateService.deleteGiftCertificate(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EntityModel<CertificateReturnDTO>> updateCertificate(@PathVariable Long id,
        @RequestBody CertificateRequestDTO requestDTO){
        CertificateReturnDTO returnCertificate = certificateService.updateGiftCertificate(id,
            new GiftCertificate(requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.duration()),
            requestDTO.tagIds());

        EntityModel<CertificateReturnDTO> resource = EntityModel.of(returnCertificate);
        resource.add(linkTo(methodOn(CertificatesController.class).getCertificate(id)).withSelfRel());

        return ResponseEntity.status(OK).body(resource);
    }
}
