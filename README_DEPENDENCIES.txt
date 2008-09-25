This is a summary of what jars you may need in your situation. Abbreviated names
have been used (only the first part of the jar name).

CORE RUNTIME
Runtime assumes that you are "compiling" rules using drools-compiler.

    * drools-core - the rule engine itself.
    * mvel-2.0M2
    * optional packages:
          o xpp3-1.1.3.4.O, xstream-1.2.2 - if you are using the file based
audit feature

Note you can use the drools-core stand-alone if you are compiling "outside" your
runtime application, and deploying serialized Package or RuleBase? objects.

COMPILER - rule assembly time
Rule compiler takes rules in some textual format and prepares binary Packages of
rules for deployment. This depends on the CORE RUNTIME.

    * drools-core
    * drools-compiler - the rule compiler itself.
    * antlr3-runtime-3.0.1
    * xerces-2.4.0, xml-apis-1.0.b2 - only if you are using XML rules, if DRL
only, can skip this.
    * eclipse-jdt-core-3.4.1.v_883_R34x - only if you want to compile with
eclipse
    * janino-2.5.15 - only if you want to compile with janino

JSR-94
This is the standard api for java rules (javax.rules).

    * drools-core
    * drools-compiler
    * drools-jsr94 - the implementation of the standard
    * jsr94-1.1 - the standard API for javax.rules.

DECISION TABLES
Decision tables use spreadsheets to generate rules.

    * drools-core
    * drools-compiler - required as rules are generated and compiled
    * drools-decisiontables - contains the spreadsheet compiler for both XLS and
CSV
    * jxl-2.4.2 (jexcelapi) - for parsing Excel spreadsheets.

The most common use case is for COMPILER - this allows rules to be loaded from
their source form,
and includes the runtime engine of course.
For example, if you wanted to be able to load rules from a drl source you would
need: drools-core and the COMPILER jars mentioned above
(not xerces, and not xml-apis). You will only need optional core jars if you are
using those features.

When building from source, the dependencies are managed by maven.
In each module, there is a pom.xml file that lists the exact dependencies and
exact version numbers.
