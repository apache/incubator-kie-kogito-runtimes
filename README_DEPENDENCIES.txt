This is a summary of what jars you may need in your situation. Abbreviated names have been used (only the first part of the jar name).

CORE RUNTIME
Runtime assumes that you are "compiling" rules using drools-compiler.

    * drools-core - the rule engine itself.
    * optional packages:
          o xpp3, xstream - if you are using the file based audit feature
          o jung, concurrent, colt, commons-collections, xercesImpl - only if you are using the visualisation feature (most people do this in the plug in - so not needed).

Note you can use the drools-core stand-alone if you are compiling "outside" your runtime application, and deploying serialized Package or RuleBase? objects.

COMPILER - rule assembly time
Rule compiler takes rules in some textual format and prepares binary Packages of rules for deployment. This depends on the CORE RUNTIME.

    * drools-core
    * drools-compiler - the rule compiler itself.
    * antlr3 - only if you are using native DRL. if only using XML, you can skip this.
    * xerces, xml-apis - only if you are using XML rules, if DRL only, can skip this.
    * commons-jci-core
          o This also requires only one pair out of:
                + commons-jci-eclipse, core-3.2 - for eclipse JDT to compile the java semantics (this is default)
                + commons-jci-janino, janino - for janino. To use this option, set -Ddrools.compiler=JANINO or use PackageBuilderConfiguration? class.
    * commons-logging - used by various dependencies.
    * commons-lang - used for string manipulation in various dependencies.
    * stringtemplate, anlr-2.7.6 - used in generating semantic code.

JSR-94
This is the standard api for java rules (javax.rules). This depends on COMPILER.

    * drools-jsr94 - the implementation of the standard
    * jsr94-1.1 - the standard API for javax.rules.

DECISION TABLES
Decision tables use spreadsheets to generate rules.

    * drools-compiler - required as rules are generated and compiled
    * drools-decisiontables - contains the spreadsheet compiler for both XLS and CSV
    * jexcelapi - for parsing Excel spreadsheets.

The most common use case is for COMPILER - this allows rules to be loaded from their source form, 
and includes the runtime engine of course. 
For example, if you wanted to be able to load rules from a drl source you would need: drools-core and the COMPILER jars mentioned above 
(not xerces, and not xml-apis), and commons-jar-eclipse, core-3.2. 
You will only need optional core jars if you are using those features.

When building from source, the dependencies are managed by maven. 
In each module, there is a pom.xml file that lists the exact dependencies and exact version numbers. 
Even if you use ant to build, the dependencies will be downloaded for you based on those pom.xml files. 
