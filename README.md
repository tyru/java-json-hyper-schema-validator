# JSON Hyper Schema Validator

JSON Hyper Schema validator for java, based on the org.json API and [everit-org/json-schema](https://github.com/everit-org/json-schema).

# Examples

* JAX-RS example
  * [JSONValidationFilterImpl.java @ tyru/jaxrs-helloapp (6884db4)](https://github.com/tyru/jaxrs-helloapp/blob/6884db4b93f1ad6f2d874f0b8c54896911d5016f/src/main/java/com/github/tyru/jaxrshelloapp/filter/JSONValidationFilterImpl.java)
* Spring MVC example
  * [SpringJSONValidationFilter.java @ tyru/spring-boot-helloapp (91dcbd9)](https://github.com/tyru/spring-boot-helloapp/blob/91dcbd95b5cfe66cc340861869d75939454c243b/src/main/java/com/github/tyru/spring/boot/helloapp/filter/SpringJSONValidationFilter.java)
  * [HelloApp.java @ tyru/spring-boot-helloapp (91dcbd9)](https://github.com/tyru/spring-boot-helloapp/blob/91dcbd95b5cfe66cc340861869d75939454c243b/src/main/java/com/github/tyru/spring/boot/helloapp/HelloApp.java#L34-L38)

# Motivation

[everit-org/json-schema](https://github.com/everit-org/json-schema) only supports [JSON Schema](http://json-schema.org/latest/json-schema-core.html), not [Hyper-Schema](http://json-schema.org/latest/json-schema-hypermedia.html).
So I created the validator library which:

1. Recognize JSON Hyper-Schema
1. Support JAX-RS
1. Support Spring

# Requirements

* Java SE 8
* Support JAX-RS 2.0 (Java EE 7) or later (if you want JAX-RS support)
* Support Spring MVC 4.2.0.RELEASE or later (if you want Spring MVC support)
