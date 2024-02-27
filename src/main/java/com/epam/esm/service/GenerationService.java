package com.epam.esm.service;

import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Role;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.RoleRepository;
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
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CertificateRepository giftCertificateRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  Faker faker = new Faker();
  Random random = new Random();

  public void deleteData() {
    try {
      orderRepository.deleteAll();
      giftCertificateRepository.deleteAll();
      tagRepository.deleteAll();
      userRepository.deleteAll();
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error deleting data from repositories",
          ErrorCode.DATABASE_ERROR, ex);
    }
  }

  public void generateUsers() {
    Role userRole = roleRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("Role User not found"));
    Role adminRole = roleRepository.findById(2L)
        .orElseThrow(() -> new RuntimeException("Role Admin not found"));

    try {
      IntStream.range(0, 1000)
          .mapToObj(i -> {
            User user = new User();
            String password = faker.animal().name() + faker.number().digit();
            String name = faker.name().firstName();
            String surname = faker.name().lastName();
            user.setName(name + " " + surname);
            user.setUsername(name.toLowerCase() + "_" + surname.toLowerCase());
            user.setEmail(user.getUsername() + "@epam.com");
            user.setNotCryptedPassword(password);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnable(faker.bool().bool());

            // assign roles randomly
            if (faker.bool().bool()) {
              user.getRoles().add(adminRole);
            } else {
              user.getRoles().add(userRole);
            }

            return user;
          })
          .forEach(userRepository::save);
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error generating users", ErrorCode.DATABASE_ERROR, ex);
    }
  }

  public void generateTags() {

    try {
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
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error generating tags", ErrorCode.DATABASE_ERROR, ex);
    }
  }


  public void generateCertificates() {
    try {
      List<Tag> tags = tagRepository.findAll();
      for (int i = 0; i < 5000; i++) {
        GiftCertificate certificate = new GiftCertificate();
        certificateCreation(certificate, tags);
        giftCertificateRepository.save(certificate);
      }
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error generating certificates", ErrorCode.DATABASE_ERROR, ex);
    } catch (Exception ex){
      throw new CustomizedException("Error saving certificate", ErrorCode.DATABASE_ERROR, ex);
    }

  }

  private void certificateCreation(GiftCertificate certificate, List<Tag> tags) {
    nameAssignation(certificate);
    descriptionAssignation(certificate);
    priceAssignation(certificate);
    durationAssignation(certificate);
    dateAssignation(certificate);
    tagsAssignation(certificate, tags);
  }

  private void tagsAssignation(GiftCertificate certificate, List<Tag> tags) {
    try {

      List<Tag> certificateTags = new ArrayList<>();

      assignRandomTags(tags, random, certificateTags);
      certificate.setTags(certificateTags);
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error assigning tags", ErrorCode.DATABASE_ERROR, ex);
    }
  }

  private void assignRandomTags(List<Tag> tags, Random random, List<Tag> certificateTags) {
    try {
      for (int j = 0; j < faker.number().numberBetween(0, 4);
          j++) { // A certificate can have 0-4 tags
        Tag randomTag = tags.get(random.nextInt(tags.size()));
        if (!certificateTags.contains(randomTag)) {
          certificateTags.add(randomTag);
        }
      }
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error assigning random tags", ErrorCode.DATABASE_ERROR, ex);
    }
  }

  private static String maximumLengthChecking(String name) {
    if (name.length() > 200) {
      name = name.substring(0, 200);
    }
    return name;
  }

  private void durationAssignation(GiftCertificate certificate) {
    certificate.setDuration(faker.number().numberBetween(10L, 101L));
    //generating random duration between 10 and 100

  }

  private void priceAssignation(GiftCertificate certificate) {
    certificate.setPrice(faker.number().randomDouble(2, 20, 1000));
    //generating random price between 20 and 100
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
