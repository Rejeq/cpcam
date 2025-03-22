### Building
To build the libraries, execute command:

```sh
ANDROID_NDK=~/Android/Sdk/ndk/28.0.13004108 ./run.sh
```

After running this command, the ./targets/ directory will be created.

The `ndk_dir` can be set to any version, but it must be kept in sync with
`ProjectConfig.ndkVersion` in the `build-logic` module.
