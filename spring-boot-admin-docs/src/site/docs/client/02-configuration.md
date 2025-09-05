---
sidebar_custom_props:
  icon: 'configuration'
---

# Configuration

In addition to discovering and registering services, Spring Boot Admin makes use of metadata to control how individual
clients are handled by the server.
Metadata allows you to fine-tune the behavior of the admin server for each registered service and can influence how
services are displayed, monitored, or interacted with.
A set of well-defined metadata attributes is recognized and evaluated directly on the server side.
Because the Spring Boot Admin client itself relies on the same Spring Cloud interfaces, these metadata properties can
also be applied consistently on the client, ensuring
a unified configuration approach across your entire system.

__Instance metadata options__

| Property name                   | Description                                                                                                                          |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `tags.*`                        | Tags as key-value-pairs to be associated with this instance.                                                                         |
| `group`                         | Assign a group name. Used in UI to aggregate instances not by application but by assigned group.                                     |
| `hide-url`                      | Hide URLs of the instance in UI. Useful, when running in a cluster, exposing a non routable URL.                                     |
| `disable-url`                   | Disables links of this instance in UI. Useful, when the URL does not point to a UI.                                                  |
| `service-url`                   | Override the service url of the registered service. Allows to specify the actual URL to the UI. This does not affect management url. |
| `user.name`<br/>`user.password` | Credentials being used to access the endpoints.                                                                                      |
