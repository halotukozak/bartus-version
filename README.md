Another git version plugin for Mill build tool.\
Like [versionFile](https://mill-build.org/mill/contrib/versionfile.html), but automatically gets the version from git
tags. \
Like [git](https://github.com/joan38/mill-git), but with `bump` and `set*` commands.

I've merged the best of both plugins and added some features.

BSP generation:

```bash
./mill mill.bsp.BSP/install
```

Intellij files:

```bash
./mill mill.idea.GenIdea/
```