1- Migrate to Spring Boot
    - Modify pom file— define the parent
    - Database configuration— Check if it connects to the database
(it does not call database.sql)


Things to do:
- [ ] Apply Hashset to tag lists and tag names in certificate filtering to avoid checking duplicates
- [ ] Read https://spring.io/guides/gs/rest-hateoas/
- Apply pagination and HATEOAS
- Can a user buy the same certificate several times?
- Do tests

Task things to do

- [ ] Get the most widely used tag of a user with the highest cost of all orders.
  - [ ] Create separate endpoint for this query.
  - [ ] Demonstrate SQL execution plan for this query (explain).
- [ ] Pagination should be implemented for all GET endpoints. Please create a flexible and non-erroneous solution. Handle all exceptional cases.
- [ ] Support HATEOAS on REST endpoints.

As for why it's better to return a DTO instead of a model:
- Abstracts away the data layer - A key function of a DTO is to separate concerns by acting as an intermediary between the data access and presentation layers of a software application. The client or API consumer only gets the data that it needs and in a defined/constant structure.
- Prevents leaky abstractions - You may not want to expose all the information provided by your data model, only a subset of it. For example, you might not want to send a user's password or some other sensitive data. Using a DTO allows you to include just the data you want to return.
- Performance - By returning only needed data, you can reduce the amount of data sent over the network and thus improve the performance of your application.
- Easier to Modify - If you expose your data model directly to clients, it becomes harder to change this model. Clients may depend on fields that they should not. If you later need to remove these fields, it can be problematic. A DTO can act as a stable interface, isolating clients from changes in the underlying model.
