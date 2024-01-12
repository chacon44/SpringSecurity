1- Migrate to Spring Boot
    - Modify pom file
        - define the parent
    - Database configuration
        - Check if it connects to the database (it does not call database.sql)


Things to do:
- [ ] Apply Hashset to tag lists and tag names in certificate filtering
- [ ] Read https://spring.io/guides/gs/rest-hateoas/

Task things to do
- [x] Change single field of gift certificate (e.g. implement the possibility to change only duration of a certificate or only price).
- [x] Add new entity User.
  - [x] implement only get operations for user entity.
- [x] Make an order on gift certificate for a user (user should have an ability to buy a certificate).
- [x] Get information about user’s order: cost and timestamp of a purchase..
  - [x] The order cost should not be changed if the price of the gift certificate is changed.
- [ ] Get the most widely used tag of a user with the highest cost of all orders.
  - [ ] Create separate endpoint for this query.
  - [ ] Demonstrate SQL execution plan for this query (explain).
- [x] Search for gift certificates by several tags (“and” condition).
- [ ] Pagination should be implemented for all GET endpoints. Please, create a flexible and non-erroneous solution. Handle all exceptional cases.
- [ ] Support HATEOAS on REST endpoints.
