Setup MAC for Native image build
================================

1. install sdkman: curl -s "https://get.sdkman.io" | bash 
2. install brew: /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
3. manually install Graal 22.3-11: brew install --cask graalvm/tap/graalvm-ce-java11 
4. link Graal inside sdk: sdk install java 22.3.r11-grl /Library/Java/JavaVirtualMachines/graalvm-ce-java11-22.3.0/Contents/Home 
5. cd kogito-apps/jitexecutor 
6. use Graal vm: sdk use java 22.3.r11-grl 
7. issue build: mvn clean package -Pnative