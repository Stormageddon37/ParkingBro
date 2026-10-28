let
  # Pin nixpkgs-unstable (newest SDK platforms available). Impure fetch is fine for a dev shell.
  pkgs = import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") {
    config.allowUnfree = true;
    config.android_sdk.accept_license = true;
  };

  android = pkgs.androidenv.composeAndroidPackages {
    platformVersions = [ "36" "37" ];
    buildToolsVersions = [ "36.0.0" "35.0.0" ];
    includeEmulator = false;
    includeSystemImages = false;
    includeNDK = false;
  };

  sdk = android.androidsdk;
in
pkgs.mkShell {
  buildInputs = [ pkgs.jdk17 sdk pkgs.android-tools ];
  JAVA_HOME = "${pkgs.jdk17}";
  ANDROID_SDK_ROOT = "${sdk}/libexec/android-sdk";
  ANDROID_HOME = "${sdk}/libexec/android-sdk";
}
