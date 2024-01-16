package com.epam.esm.service;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.repository.UserRepository;
import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {

  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CertificateRepository giftCertificateRepository;

  Faker faker = new Faker();
  Random random = new Random();

  public void deleteData() {
    orderRepository.deleteAll();
    giftCertificateRepository.deleteAll();
    tagRepository.deleteAll();
    userRepository.deleteAll();
  }

  public void generateUsers(){

    IntStream.range(0, 1000)
        .mapToObj(i -> {
          User user = new User();
          user.setName(faker.name().fullName());
          return user;
        })
        .forEach(userRepository::save);
  }

  public void generateTags() {

    IntStream.range(0, 1000).forEach(i -> {
      String tagName = switch (i % 4) {
        case 0 -> "Book genre: " + faker.book().genre();
        case 1 -> "Favourite artist: " + faker.artist().name();
        case 2 -> "Food: " + faker.food().ingredient();
        case 3 -> "Book title: " + faker.book().title();
        default -> throw new IllegalStateException();
      };
      Tag tag = new Tag();
      tag.setName(tagName);

      tagRepository.save(tag);
    });
  }


  public void generateCertificates() {

    GiftCertificate certificate = new GiftCertificate();

    for (int i = 0; i < 5000; i++) {

      certificateCreation(certificate);
      giftCertificateRepository.save(certificate);
    }
  }

  private void certificateCreation(GiftCertificate certificate) {
    nameAssignation(certificate);
    descriptionAssignation(certificate);
    priceAssignation(certificate);
    durationAssignation(certificate);
    dateAssignation(certificate);
    tagsAssignation(certificate);
  }

  private void tagsAssignation(GiftCertificate certificate) {
    List<Tag> tags = tagRepository.findAll();
    List<Tag> certificateTags = new ArrayList<>();

    assignRandomTags(tags, random, certificateTags);
    certificate.setTags(certificateTags);
  }

  private void assignRandomTags(List<Tag> tags, Random random, List<Tag> certificateTags) {
    for (int j = 0; j < faker.number().numberBetween(0, 4); j++) { // A certificate can have 0-4 tags
      Tag randomTag = tags.get(random.nextInt(tags.size()));
      if (!certificateTags.contains(randomTag)) {
        certificateTags.add(randomTag);
      }
    }
  }
  private static String maximumLengthChecking(String name) {
    if (name.length() > 200) {
      name = name.substring(0, 200);
    }
    return name;
  }
  private void durationAssignation(GiftCertificate certificate) {
    certificate.setDuration(faker.number().randomNumber()); // Set random number as duration
  }

  private void priceAssignation(GiftCertificate certificate) {
    certificate.setPrice(faker.number().randomDouble(2, 20, 1000)); //generating random price between 20 and 100
  }

  private void descriptionAssignation(GiftCertificate certificate) {
    String descriptionName = faker.chuckNorris().fact();
    descriptionName = maximumLengthChecking(descriptionName);
    certificate.setDescription(descriptionName);
  }

  private void nameAssignation(GiftCertificate certificate) {
    String certificateName = faker.animal().name();
    certificateName = maximumLengthChecking(certificateName);
    certificate.setName(certificateName);
  }

  private static void dateAssignation(GiftCertificate certificate) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    long minDay = LocalDate.of(2010, 1, 1).toEpochDay();
    long maxDay = LocalDate.of(2023, 12, 31).toEpochDay();
    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
    LocalDate randomDate = LocalDate.ofEpochDay(randomDay);

    String createDate = dtf.format(randomDate.atStartOfDay());

    certificate.setCreateDate(createDate);
    certificate.setLastUpdateDate(createDate);
  }
}
