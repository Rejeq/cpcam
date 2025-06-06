# Cpcam

![cpcam Logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp)

Cpcam is an application designed for high-performance camera streaming to remote
endpoints. Built with modern Android development practices, it supports various
media protocols and codecs for flexible streaming solutions.

> ## ⚠️ Disclaimer
>
> This application is currently in early development phase. Many features are
> still under implementation and may not be fully functional.

## Configuration

The application can be configured through the settings screen, allowing you to:
- Connecting to a different remote endpoints, currently supported:
  + OBS (via obs-websocket)
- Select streaming protocols, currently supported:
  + MPEGTS
  + MJPEG
  + RTSP
  + RTP
  + HLS
- Configure codecs, currently supported:
  + Mediacodec (H264, VP8, VP9, AV1, MPEG4, HEVC)
  + MJPEG

## Building

1. Clone the repository:
   ```bash
   git clone https://github.com/rejeq/cpcam
   ```

2. Build native libraries (POSIX environment required)
    ```bash
    bash jni_deps/run.sh
    ```

3. Configure `keystore.properties` for release builds:
   If you want to use default android debug key:
   - Copy `keystore_base.properties` to `keystore.properties`
   - Specify keystore file location (`storeFile` property)

4. Build and run the application
   ```bash
   # Linux
   ./gradlew installRelease
   # Windows
   gradlew installRelease
   ```

## Project Structure

```
cpcam/
├── app/               # Application entry point
├── core/
│   ├── camera/        # Camera handling
│   ├── common/        # Shared utilities
│   ├── data/          # Data and models management
│   ├── device/        # Wrappers for android-specific functionality
│   ├── endpoint/      # Streaming endpoints
│   ├── stream/        # Stream data handling
│   └── ui/            # Common UI components
├── feature/
│   ├── main/          # Main screen
│   ├── settings/      # Settings screen
│   ├── about/         # About screen
│   └── service/       # Background services
└── build-logic/       # Common build logic
```
