[open-issues]: https://github.com/it-at-m/refarch/issues
[new-issue]: https://github.com/it-at-m/keycloak-plugins/issues/new/choose
[license]: ./LICENSE
[new-issue-shield]: https://img.shields.io/badge/new%20issue-blue?style=for-the-badge
[made-with-love-shield]: https://img.shields.io/badge/made%20with%20%E2%9D%A4%20by-it%40M-yellow?style=for-the-badge
[license-shield]: https://img.shields.io/github/license/it-at-m/refarch?style=for-the-badge
[itm-opensource]: https://opensource.muenchen.de/

# keycloak-plugins

[![New issue][new-issue-shield]][new-issue]
[![Made with love by it@M][made-with-love-shield]][itm-opensource]
[![GitHub license][license-shield]][license]

Collection of different [Keycloak](https://www.keycloak.org/) plugins.

## DEPRECATED: Migrated to single Repos

This repository is deprecated and the keycloak plguins were split up into singple repositories:
- https://github.com/it-at-m/keycloak-authority-mapper-plugin
- https://github.com/it-at-m/keycloak-custom-otp-plugin
- https://github.com/it-at-m/keycloak-require-http-header-authenticator-plugin
- https://github.com/it-at-m/keycloak-require-role-authenticator-plugin
- https://github.com/it-at-m/keycloak-username-from-login-hint-authenticator-plugin

## Components

- [authority-mapper](./authority-mapper): plugin for mapping Keycloak permissions into user info authorities claim
- [custom-otp](./custom-otp)
- [require-http-header-authenticator](./require-http-header-authenticator): plugin for requiring a specific header and value being present or not present
- [require-role-authenticator](./require-role-authenticator)
- [username-from-login-hint-authenticator](./username-from-login-hint-authenticator): plugin for extracting username from login_hint header

### Built With

- OpenJDK 21
- Keycloak 26

## Roadmap

See the [open issues][open-issues] for a full list of proposed features (and known issues).

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Open an issue with the tag "enhancement"
2. Fork the Project
3. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

More about this in the [CODE_OF_CONDUCT](./.github/CODE_OF_CONDUCT.md) file.

## License

Distributed under the MIT License. See [LICENSE](LICENSE) file for more information.


## Contact

it@M - opensource@muenchen.de
