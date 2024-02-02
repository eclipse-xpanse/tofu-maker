<p align='center'>
<a href="https://github.com/eclipse-xpanse/tofu-maker/actions/workflows/ci.yml" target="_blank">
    <img src="https://github.com/eclipse-xpanse/tofu-maker/actions/workflows/ci.yml/badge.svg" alt="build">
</a>
<a href="https://opensource.org/licenses/Apache-2.0" target="_blank">
    <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="coverage">
  </a>
</p>

# tofu-maker

A spring-boot-based project which aims to provide a RESTful API for OpenTofu CLI. It provides three different modes of 
execution

1. Scripts in a directory
2. Scripts in REST request body
3. Scripts in a GIT repo

## Modes of OpenTofu Script Execution

### Scripts in a Directory

Pass the open tofu root folder name in the request and tofu-maker will execute the requested 
OpenTofu method on
this directory.

#### OpenTofu Root module Folder

If we intend to use this mode of script execution, then we must ensure the scripts root directory is
accessible to tofu-maker runtime.

The `tofu-maker` application must have access to the root module folder under which each sub-folder is a module
used per API request.

The default root folder where all module sub-folders will exist is the **temp** folder of the user running the server.

This configuration can be changed by updating the `opentofu.root.module.directory` property.

### Scripts in the Request Body

All files needed for open tofu execution can be passed as strings to API and tofu-maker will 
automatically execute
the files and return open tofu execution result.

### Scripts in GIT Repo

If the scripts are present in a GIT repo, then we can directly pass the details of the GIT repo. OpenTofu-boot will 
clone the repo and execute the scripts and then return the result. 

> [!NOTE]
> Currently supports only repos that can be cloned without authentication and also with HTTP(S) only.


## Supported API Methods

* deploy - This wraps the OpenTofu `plan` and `apply` methods.
* destroy - This wraps the OpenTofu `destroy` method.
* validate - This wraps the OpenTofu `validate` method.
* plan - This wraps the OpenTofu 'plan' method.
* healthCheck - This method returns the status of the tofu-maker application

> [!NOTE]
> All opentofu related methods above support both modes of operation mentioned in
> section [Modes of OpenTofu Script Execution](#modes-of-opentofu-script-execution)

## Supported Authentication Methods

1. None - By default the API methods can be accessed without any authentication.
2. oauth - This can be enabled by activating spring profile - `oauth`

## Available Configurations

The below property names can be changed in the following ways

1. passing the property values to the server startup command as ``--${property-name}=${property-value}``
2. Setting corresponding environment variables before starting the server.

| property name                            | environment variable                     | default value                                     | description                                                                                                            |
|------------------------------------------|------------------------------------------|---------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| opentofu_binary_path                     | OPENTOFU_BINARY_PATH                     | OpenTofu available on syspath                     | The path to the opentofu binary                                                                                        |
| opentofu.root.module.directory           | OPENTOFU_ROOT_MODULE_DIRECTORY           | /tmp on Linux<br/>\AppData\Local\Temp on Windows  | The path to the parent directory where all opentofu module directories will be stored at as subdirs                    |
| log.opentofu.stdout.stderr               | LOG_OPENTOFU_STDOUT_STDERR               | true                                              | Controls if the command execution output must be logged. If disabled, the output is only returned in the API response  |
| opentofu.log.level                       | OPENTOFU_LOG_LEVEL                       | INFO                                              | Controls the log level of the opentofu binary. Allowed values are INFO, DEBUG, TRACE, WARN and ERROR                   |
| authorization.token.type                 | AUTHORIZATION_TOKEN_TYPE                 | JWT                                               | Authorization server authentication Type, allowed values: OpaqueToken or JWT                                           |
| authorization.server.endpoint            | AUTHORIZATION_SERVER_ENDPOINT            |                                                   | The endpoint value of the authorization server                                                                         |
| authorization.api.client.id              | AUTHORIZATION_API_CLIENT_ID              |                                                   | The ID value of the authorization server API client                                                                    |
| authorization.api.client.secret          | AUTHORIZATION_API_CLIENT_SECRET          |                                                   | The secret value of the authorization server API client                                                                |
| authorization.swagger.ui.client.id       | AUTHORIZATION_SWAGGER_UI_CLIENT_ID       |                                                   | The ID value of the authorization server swagger-ui client                                                             |
| otel.exporter.otlp.endpoint              | OTEL_EXPORTER_OTLP_ENDPOINT              | http://localhost:4317                             | URL of the OTEL collector                                                                                              |
| clean.workspace.after.deployment.enabled | CLEAN_WORKSPACE_AFTER_DEPLOYMENT_ENABLED | true                                              | Whether to clean up the workspace after deployment is done,allowed values: true or false. Default value is true        |

## Run Application

### Local Development

Server can be compiled and started as below

If you have a fully configured OAuth instance running on your local system, then you can use the below way to
start the application. The variables and their descriptions that need to be passed during oauth startup are recorded in
the Available Configurations table.

#### From Command Line

1.Start with oauth

```shell
./mvmw clean install -DskipTests
$ java -jar target/tofu-maker-*.jar\
--spring.profiles.active=oauth \
--authorization.token.type=${token-type} \
--authorization.server.endpoint=${server-endpoint} \
--authorization.api.client.id=${client-id} \
--authorization.api.client.secret=${client-secret} \
--authorization.swagger.ui.client.id=${swagger-ui-cleint-id}
```

2.Start without oauth

```shell
./mvmw clean install -DskipTests
$ java -jar target/tofu-maker-*.jar
```

#### From IDE

1.Start with oauth

    1.Set values for the authorization related variables in the application-oauth-properties configuration file, 
      and specify the configuration file for loading oauth in the application-properties configuration file, 
      start the main application.
    2.Or the oauth related variables configuration can be added to IDE and the main application can be executed directly
      to launch the application.

2.Start without oauth

    Simply start the main application.

API can be accessed using the following URLs

```html
http://localhost:9090
http://localhost:9090/swagger-ui/index.html
```

### Production

#### Docker Image

This is the recommended way. Docker images can be pulled from GitHub Packages as
mentioned [here](https://github.com/eclipse-xpanse/tofu-maker/pkgs/container/tofu-maker).
All configuration parameters can be passed as environment variables to the container.

#### Run Jar

We also deliver a jar for each release and can be found in the asests list of each
release [here](https://github.com/eclipse-xpanse/tofu-maker/releases).
The jar can be started as mentioned in the same way we do for local development.

### Observability

`tofu-maker` provides an option to enable observability using [openTelemetry](https://opentelemetry.io/). This can
be enabled by starting the runtime with profile `opentelemetry`.

By enabling this profile, the application forwards metrics, traces and logs to the `otel-collector`.

By default, the application sends all telemetry data to `http://localhost:4317` and this can be changed by updating the
value of `otel.exporter.otlp.endpoint` configuration property.

## Dependencies File

All third-party related content is listed in the [DEPENDENCIES](DEPENDENCIES) file.