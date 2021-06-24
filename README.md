# NickAPI

[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)

A nick api on packet level with fully support for uuid, skin or username change. Everything happens clientside and with
the functionality to declare filters, to check if the target will see the fake identity.

## Features

- UUID, username and skin seperated can be set
- Everything based on Reflection - Multi version support
- without Dependencies - uses [TinyProtocol](https://github.com/dmulloy2/ProtocolLib)

## Usage

You can find a implementation for the
NickAPI [here](plugin/src/main/java/eu/miopowered/nickapi/plugin/NickPluginImplementationTest.java).

## Installation

### Gradle

```groovy
maven {
    name "miopowered-repo"
    url "https://repo.miopowered.eu/releases"
}

dependencies {
    implementation('eu.miopowered.nickapi:api:1.0')
}
```

### Maven

```xml

<repository>
    <id>miopowered-repo</id>
    <url>https://repo.miopowered.eu/releases</url>
</repository>

<dependency>
    <groupId>eu.miopowered.nickapi</groupId>
    <artifactId>api</artifactId>
    <version>1.0</version>
</dependency>
```

## Proof of concept

You also can find my proof of concept [here](plugin/src/main/java/eu/miopowered/nickapi/plugin/NickPlugin.java).

## Known issues

- Before quiting as nicked, you need to remove him from the tab via

``NickUpdater#removeFromTab(nickUser.viewers(), nickUser.fakeIdentity().uniqueId())``

- Not possible that the player can see this fake skin

## License

[MIT](LICENSE)