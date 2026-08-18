# Contributing

Before making a substantial change, open an [issue](https://github.com/Despical/ParticleText/issues/new) and discuss the intended behavior with the repository owner.

Please follow the project [Code of Conduct](CODE_OF_CONDUCT.md) in all interactions.

## Pull request process

- Use spaces instead of tabs and respect the existing code style and package boundaries.
- Keep changes focused; do not reformat or reorganize unrelated files.
- Do not change release version numbers unless the pull request specifically requires it.
- Preserve the cached configuration model and keep Bukkit API access on the server thread.
- Document new commands, permissions, configuration keys, and placeholders.
- Run `./gradlew build` successfully before submitting the pull request.
- Confirm the shaded JAR is produced under `build/libs/`.

If you are looking for work, review the [open issues](https://github.com/Despical/ParticleText/issues) first to avoid duplicate efforts.

## Bug reports

- Search for an existing report before opening a new one.
- Reproduce the issue on the latest Particle Text version.
- Include the Particle Text, Server, Minecraft, Java, and PlaceholderAPI versions when relevant.
- Include steps to reproduce and the smallest useful server log excerpt.
- Use the issue tracker for code defects, not general server administration support.

## Additional resources

- [GitHub pull request documentation](https://docs.github.com/en/pull-requests)
- [Paper plugin development documentation](https://docs.papermc.io/paper/dev/)
