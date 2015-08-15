## sonar-text-plugin
A free and open-source plugin for SonarSource's Sonarqube product that lets you create rules to flag issues in text files.

### Why use this plugin

The uses that I had in mind when building this were:

* Common software configuration mistakes get made in our (dev|test|prod).properties files frequently enough to warrant automated detection. Examples are:
 * JDBC URLs using server names or IP addresses when a DNS CName is more appropriate.
 * Fault-tolerant URLs used with active/active and active/standby infrastructure that list only one of the nodes when it should list all of them (tip: prefer using JNDI-like lookups instead of listing all nodes if you have more than a couple of pieces of software doing this).
* Unproven idea: Create Sonar issues on any library dependencies that have a known issue.
 * Build tools such as Maven typically have commands available to generate a list of direct and indirect dependencies. You could run that command prior to the Sonarqube scan, pipe the output to a text file, and maintain a set of rules that apply to that file.
 * This approach wouldn't "fingerprint" Jar libraries or use a public list of issues like OWasp's Dependency-Check does BUT it would give you an easy way to flag versions of libraries from your own portfolio that teams should not be using any more.

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

After you've done the above you'll be ready to run a scan and see the first rule work.
