# About

This is a dictionary matching component for the [STW Thesaurus for Economics](http://zbw.eu/stw/version/latest/about) (STW).
The software builds upon the finite-state-automaton (FSA) text filtering tool [monq](https://github.com/HaraldKi/monqjfa).

In particular, STWFSA implements generation of regular expressions 
to recognize preferred terms and alternative terms of the STW.
A number of test cases are provided to assure correct recognition of several
patterns.

STWFSA is meant to be integrated into more complex automatic
subject indexing pipelines, where it may be combined with other dictionary
matching tools.
Appropriately configured, you can run STWFSA as a __candidate generator__ and 
__combine it with machine learning__ techniques, which especially allows to reject ambiguous matches.

Please visit [zbw.eu](https://www.zbw.eu) for more information about ZBW's automatic subject indexing working group.

Author: [Martin Toepfer](https://www.zbw.eu/de/forschung/science-2-0/martin-toepfer/), 2017-2018

ZBW - Leibniz Information Centre for Economics

# Application

Before testing and building STWFSA, you should adapt the file *pom.xml*, e.g., set *STW_PTH*.
When you have built STWFSA successfully and monq is on your classpath, you can run the tool like

```
set STW_DIR=~/kb/stw

java -cp zbw-a1-match-fsa-$VERSION.jar eu.zbw.stwfsa.app.StwRecApp -in content_unlabeled.tsv -out predicted.tsv
```

The directory *STW_DIR* must contain the file _stw.nt_.

Add the argument `-info` to print offsets and matching text. The call
`java -cp zbw-a1-match-fsa-$VERSION.jar eu.zbw.stwfsa.app.StwRecApp -help`
explains all arguments and usage in more detail.

You may want to have a look at [StwRecServe](src/main/java/eu/zbw/stwfsa/app/StwRecServe.java) as a starting point
for offering dictionary matching as a webservice. 

## Input and Output

STWFSA reads and writes tab-separated (TSV) data.

### Input

Two columns. For each row: document id, content (short-text, e.g., title)

### Output

default: document id, concept id

option: `-compressed`: document id, list of concept ids (tab-separated)

option: `-info`:  document id, concept id, begin, end, covered text

Please note, option `info` has precedence over option `compressed`.

## Programmatic Use

If you want to use STWFSA programmatically, please have a look at [StwAnnotator](src/main/java/eu/zbw/stwfsa/annotator/StwAnnotator.java).

# Change Notes

## v0.3-SNAPSHOT

- add simple server example

### v0.2 - May 14, 2018

This is the first entry of the public changelog.

