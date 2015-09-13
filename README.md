## sonar-text-plugin

A free and open-source Community plugin for SonarSource's Sonarqube product that lets you create rules to flag issues in text files. Currently it supports raising issues:
* When text matching a regex is present (line-by-line scan & dot does not match all)
* When text matching regex 'A' is present require that text matching regex 'B' also be present (dot matches all, expressions can describe multiple lines)

### Screenshots

<img src=screenshots/Screenshot_IssueDrilldown_DependencyIssue.png>

Other screenshots are available by browsing the "<a href="screenshots/">/screenshots</a>" directory in this project.

### Why use this plugin

The uses that I had in mind when building this were:

* Common software configuration mistakes get made in our (dev|test|prod).properties files frequently enough to warrant automated detection. Examples are:
 * JDBC URLs using server names or IP addresses when a DNS CName is more appropriate.
 * Fault-tolerant URLs used with active/active and active/standby infrastructure that list only one of the nodes when it should list all of them (tip: prefer using JNDI-like lookups instead of listing all nodes if you have more than a couple of pieces of software doing this).
 * See also <a href="https://github.com/racodond/sonar-jproperties-plugin">David Racodon's Java Properties plugin</a>. It is designed to understand Java Properties files and look for issues such as duplicate variable definitions and empty variable definitions.

* Create Sonar issues that flag library dependencies that have a known deficiency, mustn't be used outside of 'test' scope, or mustn't be used at all due to an organizational policy or licensing concern.
 * Build tools such as Maven typically have commands available to generate a list of direct and indirect dependencies. You could run that command prior to the Sonarqube scan, pipe the output to a text file, and maintain a set of rules that apply to that file.
 * This approach wouldn't "fingerprint" Jar libraries or use a public list of issues like OWasp's Dependency-Check does BUT it would give you an easy way to flag versions of libraries from your own portfolio that teams should not be using any more.

* If your projects contain a shell script that sets environment variables or that does some other work you could use this plugin to run a regular expression search of those files to detect occurrences of some known problematic practice. This might get you by until a shell scripting language plugin is available (I don't think one is at this time).
 
### To install the plugin:
1. Ctrl-F for "release" on this Github page and click that link
2. Download the plugin Jar file
3. Copy that Jar into your Sonar installation's "extensions/plugins" directory
4. Reboot Sonar

### To configure your first rule:
1. Log in to Sonar, go to Quality Profiles, find the new "Text" section, and create a new profile under that
2. Make the new Quality Profile the default Test profile
3. Add an initial rule to the new Quality Profile's ruleset. Do this by first finding the _inactive_ "Simple Regex Match" rule. Click on that rule.
4. Click the "create" button that appears next to the words "CUSTOM RULES" at the bottom of the rule definition
5. Define your rule and save it
6. Activate the new rule in a quality profile
7. Double-check to ensure that the list of extensions for the 'text' language includes the extension of the file that your rule is looking for.
8. Double-check to ensure that your "sonar.sources" path will include the file to be scanned. If this path is set to "src/main/java" then Sonar won't scan files at the root of your project or in "src/main/resources".

After you've done the above you'll be ready to run a scan and see the first rule work.
