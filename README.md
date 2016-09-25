# JSON Hyper Schema Validator

JSON Hyper Schema validator for java, based on the org.json API and [everit-org/json-schema](https://github.com/everit-org/json-schema).

# Examples

See [tyru/jaxrs-helloapp (f052d59)](https://github.com/tyru/jaxrs-helloapp/tree/f052d591c553e7284eade8a7a163d4b9b3d3237d).

# Motivation

[everit-org/json-schema](https://github.com/everit-org/json-schema) only supports [JSON Schema](http://json-schema.org/latest/json-schema-core.html), not [Hyper-Schema](http://json-schema.org/latest/json-schema-hypermedia.html).
So I created the validator library which:

1. Recognize JSON Hyper-Schema
1. Support JAX-RS
1. Support Spring

# Requirements

* Java SE 8
* If you want JAX-RS support
  * JAX-RS 2.0 (Java EE 7)
* If you want Spring support
  * Spring ver x.y.z
