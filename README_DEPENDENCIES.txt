This is a summary of what jars you may need in your situation. Abbreviated names
have been used (only the first part of the jar name).
API
    * jaxb-impl 2.1.9
    * jaxb-xjc 2.1.9
    * milyn-smooks-javabean 1.1
    * jxls-reader 0.9.6
    * xstream 1.3.1 
    * jms 1.1

CORE RUNTIME
Runtime assumes that you are "compiling" rules using drools-compiler.

    * drools-core - the rule engine itself.
    * drools-api
    * mvel2-2.0.12
    * joda-time-1.6
    * optional packages:
          o xpp3_min-1.1.3.4.0, xstream-1.3.1 - if you are using the file based
audit feature

Note you can use the drools-core stand-alone if you are compiling "outside" your
runtime application, and deploying serialized Package or RuleBase? objects.

COMPILER - rule assembly time
Rule compiler takes rules in some textual format and prepares binary Packages of
rules for deployment. This depends on the CORE RUNTIME.

    * drools-core
    * drools-api
    * drools-compiler - the rule compiler itself.
    * antlr3-runtime-3.1.1
    * xerces-2.9.1, xml-apis-2.0.2 - only if you are using XML rules, if DRL
only, can skip this.
    * eclipse-jdt-core-3.4.2.v_883_R34x - only if you want to compile with
eclipse
    * janino-2.5.15 - only if you want to compile with janino

JSR-94
This is the standard api for java rules (javax.rules).

    * drools-core
    * drools-api
    * drools-compiler
    * drools-decisiontables
    * drools-jsr94 - the implementation of the standard
    * jsr94-1.1 - the standard API for javax.rules.

DECISION TABLES
Decision tables use spreadsheets to generate rules.

    * drools-core
    * drools-api
    * drools-compiler - required as rules are generated and compiled
    * drools-templates
    * drools-decisiontables - contains the spreadsheet compiler for both XLS and
CSV
    * jxl-2.4.2 (jexcelapi) - for parsing Excel spreadsheets.

ANT
Ant tasks for creating rule and knowledge packages and to verify rules.

    * drools-api
    * drools-core
    * drools-compiler
    * drools-decisiontables
    * ant 1.6.5
    * ant-nodeps 1.6.5
    * xstream 1.3.1
    * drools-verifier - If you are using the verifier ant task

SERVER
    * drools-compiler
    * xstream 1.3.1 
    * stax 1.2.0
    * jettison 1.0.1
    * servlet-api 2.3

TEMPLATES
    * drools-core
    * drools-compiler

VERIFIER
    * drools-api
    * drools-compiler
    * xstream 1.3.1 - If you are using the XML report

DROOLSDOC
Creates a PDF documentation from DRL
    * itext 2.1.2



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
