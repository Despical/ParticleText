# Contributing

Discuss substantial changes in a [GitHub issue](https://github.com/Despical/ParticleText/issues)
before submitting a pull request. Follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Development

Use Java 25 and the checked-in Gradle wrapper. Run:

```shell
./gradlew clean build javadocJar --no-daemon --console=plain
```

Keep four-space Java indentation, explicit braces, UTF-8, LF endings, GPL headers,
and the existing Javadoc conventions. Keep services focused on one responsibility.
Add configurable player-facing text to `messages.yml`; document every new setting
and its bounds in `config.yml`. Insert user values through unparsed placeholders.

## Runtime behavior

Bukkit calls, renderer mutations, font rasterization, and menus run on the server
thread. Update checks use asynchronous HTTP requests. Keep saved text records compatible and
preserve existing renderer state when loading or persistence fails.

Include meaningful regression tests for behavior changes. For integration changes,
check a separate Paper server with both optional PlaceholderAPI states when applicable.
Describe what was checked and any remaining runtime or visual limitation in the PR.

Do not change versions, reformat unrelated files, or include generated builds,
server worlds, credentials, or local IDE state.
