# HitHealth development

HitHealth is an open-source project under the MIT License.

- Preserve the public behavior, multilingual documentation, and Java 21 bytecode target.
- Do not change the project or release version unless the user explicitly requests a version update or a versioned release. Other repositories have independent versions; do not bump them as a side effect.
- Keep plugin metadata, commands, permission nodes, and data-path documentation consistent with the implementation.
- Run `mvn -B clean verify` before delivering a build. Unit tests require no running Minecraft server.
- Run the optional Paper probe only with explicit approval to start the server and accept its EULA. Do not publish, push, or deploy without authorization.
