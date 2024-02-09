#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FILE_NAME="getting-started"
SRC_DIR="$SCRIPT_DIR/src/content/adocs/en/$FILE_NAME.adoc"
XML_DIR="$SCRIPT_DIR/src/content/adocs/en/$FILE_NAME.xml"
OUT_DIR="$SCRIPT_DIR/docs/tutorial-basics/$FILE_NAME.md"

# Convert all .adoc files to .md
XML=$(asciidoc -b docbook --out-file - $SRC_DIR)
echo $XML > $XML_DIR
echo $XML | pandoc --wrap=preserve --preserve-tabs=true -t gfm -f docbook - > $OUT_DIR

