# IDE Setup Instructions

Before you start contributing, please follow the instructions below to setup a code style for an IDE of your choice.

## Eclipse Setup

Open the _Preferences_ window, and then navigate to _Java -> Code Style -> Formatter_.
Click _Import_ and then select the `kogito-ide-config/src/main/resources/eclipse-format.xml` file in the `ide-configuration` directory.

Next navigate to _Java -> Code Style -> Organize Imports_.
Click Import and select the `kogito-ide-config/src/main/resources/eclipse.importorder` file.

## IDEA Setup

Open the _Preferences_ window (or _Settings_ depending on your edition), navigate to Plugins and install the [Eclipse Code Formatter Plugin](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter) from the Marketplace.

Restart your IDE, open the _Preferences_ (or _Settings_) window again and navigate to _Other Settings -> Eclipse Code Formatter_.

Select _Use the Eclipse Code Formatter_, then change the _Eclipse Java Formatter Config File_ to point to the `eclipse-format.xml`
file in the `kogito-ide-config/src/main/resources/` directory.
Make sure the _Optimize Imports_ box is ticked, and select the `eclipse.importorder` file as the import order config file.

## VScode setup 

First download the [Checkstyle for Java](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle) plugin then right click on `kogito-ide-config/src/main/resources/eclipse-format.xml` and `Set the *Checkstyle Configuration File*`. 

Full instruction can be found [here](https://code.visualstudio.com/docs/java/java-linting)
