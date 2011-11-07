ARtool v1.1.2 - Association Rule Mining Algorithms and Tools
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This is file 0README.TXT written by Laurentiu Cristofor
Last modified on February 4th, 2002



Introduction
============

ARtool is a Java application for mining frequent itemsets and
association rules in binary databases. ARtool is free software
distributed under the GNU General Public License.

If you find ARtool useful, then please let me know by sending me
email, my address is laur@cs.umb.edu. Your feedback will encourage me
to periodically update ARtool with new features. If you enhance ARtool
in any way or if you write additional tools related to ARtool, then
let me know and I will add a link to your site on ARtool's page.



List of documents included in the ARtool distribution
=====================================================

0COPYRIGHT.TXT          - ARtool copyright notice
0LICENSE                - ARtool license (the GNU GPL)
0README.TXT             - this file
0FAQ.TXT                - ARtool Frequently Asked Questions
0ASC_TUTORIAL.TXT       - tutorial for asc2db and db2asc command line
                          tools
0HISTORY.TXT            - ARtool history



Glossary of terms
=================

GUI	- Graphical User Interface
JDK	- Java Development Kit
JRE	- Java Runtime Environment
JVM	- Java Virtual Machine



How to install and execute ARtool
=================================

You need to have Sun's JDK or JRE installed, probably at least version
1.3.

To install ARtool, just unzip the ARtool binaries to some directory on
your hard drive.

To run the ARtool GUI, just type:

        java -jar ARtool.jar

or double-click on ARtool.jar (works only if you have JRE installed).

If ARtool runs out of memory during some mining operation (you see an
OutOfMemoryException message), then you need to allocate more memory
to the JVM. In the case of Sun's JDK you can do this by typing:

        java -Xmx512M -jar ARtool.jar

which will let JVM use 512MB of memory, assuming of course that you
have that much memory installed.

If you want to use the command line utilities, then you will have to
add laur.zip to your classpath. On Windows you need to have in your
autoexec.bat a line like this:

        SET CLASSPATH=.;C:\ARTOOL\BIN\LAUR.ZIP

If you use Unix, then you will have to add something like

        setenv CLASSPATH .:~/ARtool/bin/laur.zip

to your shell configuration file.

laur.zip contains the compiled versions of the laur.dm.ar, laur.rand,
and laur.tools packages.

Check the Help menu for an introduction to association rule mining
and the ARtool application.

Also note that ARtool is viewed best in a resolution of 1024x768 or
higher.

For more information check the ARtool website:

        http://www.cs.umb.edu/~laur/ARtool/

If you have a bug report then you can write me at:

        laur@cs.umb.edu



General tips for the command line tools
=======================================

 - where a database name is required, the .db extension should be
   omitted since all programs add it automatically. This means that
   all the databases must use the .db extension.

 - 0ASC_TUTORIAL.TXT contains a tutorial regarding the use of the
   asc2db and db2asc tools.

 - use db2asc to obtain a text dump of a database

 - use dbtool to set a new description for a database

EOF==================================================================
