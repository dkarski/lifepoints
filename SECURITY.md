# Security

## Reporting a vulnerability

Report vulnerabilities privately through
[GitHub security advisories](https://github.com/dkarski/lifepoints/security/advisories/new), not in public issues.
I will reply as soon as I can.

Lifepoints has no network access, permissions or accounts. The most relevant issues are in how the app is built,
signed and released.

## Verifying a download

Every release is signed with the same key. The SHA-256 fingerprint of the signing certificate is:

    468d96450f7b40612b9869e50474d43925950463bbe685587f9d3c7a116957bc

To check an APK, run `apksigner verify --print-certs lifepoints-X.Y.Z.apk` (Android SDK Build-Tools) and compare
the `SHA-256 digest`. The `.sha256` file attached to each release only confirms that the download is complete.
