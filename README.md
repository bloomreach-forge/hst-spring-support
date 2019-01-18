[![Build Status](https://travis-ci.org/bloomreach-forge/hst-spring-support.svg?branch=develop)](https://travis-ci.org/bloomreach-forge/hst-spring-support)

# HST Spring Framework Support

This project provides components which fill the gap between HST-2 framework and Spring Framework.

# Documentation (Local)

The documentation can generated locally by this command:

```bash
$ mvn clean install
$ mvn clean site
```

The output is in the ```target/site/``` directory by default. You can open ```target/site/index.html``` in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://bloomreach-forge.github.io/content-export-import/](https://bloomreach-forge.github.io/content-export-import/).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn clean install
$ find docs -name "*.html" -exec rm {} \;
$ mvn -Pgithub.pages clean site
```

The output is in the ```docs/``` directory by default. You can open ```docs/index.html``` in a browser.

You can push it and GitHub Pages will be served for the site automatically.
