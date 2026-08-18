# Security Policy

Security reports are taken seriously. Do not open a public issue for a vulnerability.

## Reporting a vulnerability

Send reports privately to:

```text
contact@despical.dev
```

Include a clear description, reproduction steps, affected version and server environment, relevant logs, and a description of the expected impact. Do not include destructive payloads, credentials, private player data, or anything that could damage a running server.

## Scope

Security-sensitive areas include:

- Command permissions and administrative actions.
- Renderer configuration persistence and reload behavior.
- MiniMessage and PlaceholderAPI input handling.
- Inventory menu event handling.
- Update checks and packaged third-party libraries.
- Particle workload limits that could affect server availability.

Only the latest public version is supported. Non-security bugs should use the normal issue tracker.

Please avoid public disclosure until a fix is available.
