Looking to report a Bug or make a feature request? Take a look [here](https://github.com/shadow578/Music_DL#issues-feature-requests-and-contributing).

---

### Thank you for your interest in contributing to Music_DL!

# Translations
Translations are currently only possible by directly editing the strings.xml file. 


# Code Contributions

__Pull requests are welcome!__
Please read follow the Code Style Guidelines below.

If you're interested in taking on [an open issue](https://github.com/shadow578/Music_DL/issues), please comment on it so others are aware.

# Forks

Forks are allowed so long they abide by [Music_DL's LICENSE](LICENSE)

When creating a fork, remember to:

- Avoid confusion with the main app and conflicts by:
    - Changing the app name (strings/app_name)
    - Changing the app icon
    - Change the 'applicationId' in build.gradle
 
# Code Style Guidelines

These are the guidelines you should follow when contributing code to Music_DL.<br>
These Guidelines outline what I think are useful rules to create readable and manageable code, tho they are always open for discussion(i'm not a professional developer after all, so what do i know :P)

- Please use Java (I'm not familiar with Kotlin (yet), and don't have the time to learn it because of University)
- Do not hardcode stuff (intent extras/actions, urls, ...), but use constants in the appropriate classes instead
- Enums in PascalCase (yes, java convention is to use ALL_UPPERCASE, but that looks ridiculous)
- include javadoc comments for:
  - __all__ classes, interfaces and enums
  - __all__ public fields, methods and constants
  - _optionally_ private fields, methods and constants
  - comments should describe the class/field/method, but don't have to be too long
- Use @Nullable / @NonNull annotations on:
  - __all__ parameters and return values of public methods
  - _optionally_ private methods
- Use Lambda expressions where java allows it
- do __not__ ignore lint warnings
