#!/bin/bash

#Timeouts in the competition
#8x8 maps: 3000
#16x16 maps: 4000
#24x24 maps: 5000
#32x32 maps: 6000
#64x64 maps: 8000
#> 64x64 maps: 12000

# $1 number of runs
# $2 result file path 
# $3 timeout

javac -cp src/:lib/jdom.jar:lib/minimal-json-0.9.4.jar src/tests/CompareAllAIsPartiallyObservable.java

java -cp src:lib/jdom.jar:minimal-json-0.9.4.jar tests.CompareAllAIsPartiallyObservable $1 $2 $3
