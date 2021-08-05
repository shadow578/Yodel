Looking to report a Bug or make a feature request? Take a look [here](https://github.com/shadow578/Yodel#issues-feature-requests-and-contributing).

---

### Thank you for your interest in contributing to Yodel!

<br>

# Translations
Translations are currently only possible by directly editing the strings.xml file and creating a PR. 


# Code Contributions

__Pull requests are welcome!__
Please read follow the Code Style Guidelines below.

If you're interested in taking on [an open issue](https://github.com/shadow578/Yodel/issues), please comment on it so others are aware.

# Forks

Forks are allowed so long they abide by [Yodel's LICENSE](LICENSE)

When creating a fork, remember to:

- Avoid confusion with the main app by:
  - Changing the app name (strings/app_name)
  - Changing the app icon
- Avoid installation conflicts by:
  - Change the 'applicationId' in build.gradle


if you want to use Yodels automatic build system, have a look [at the build guide](BUILDING.md)


# Branches

Use the following prefixes for branches:

Prefix | Function
-|-
feature/ | new features and improvements to existing features
fix/ | bugfixes\*
locale/ | for locale additions and updates


> \* sometimes, a bugfix is implemented alongside a new feature. this is ok. you do __not__ have to create an additional branch just to fix a bug. instead, just include it with your feature.


# Code Style Guidelines

These are the guidelines you should follow when contributing code to Yodel.<br>
These Guidelines outline what I think are useful rules to create readable and manageable code, tho they are always open for discussion(i'm not a professional developer after all, so what do i know :P)


- Yodel uses the [Model View ViewModel](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) pattern
- Use Kotlin as the main development language
  - I'm new to kotlin, so if i'm doing something stupid (or could do it better), please let me know
  - if you use a language concept that is not obvious, please leave a comment
- Do not hardcode stuff (intent extras/actions, urls, ...), but use constants in the appropriate classes instead
- Enums in PascalCase (convention seems to be ALL_UPPERCASE, but that looks ridiculous)
- include kdoc comments for:
  - __all__ classes, interfaces and enums
  - __all__ public fields, methods and constants
  - _optionally_ private fields, methods and constants
  - comments should describe the class/field/method, but don't have to be too long
- do __not__ ignore lint warnings
- Try to include tests for your contribution (where applicable)
  - Yodel uses [Kotest](https://kotest.io/) with JUnit for unit and instrumented tests

