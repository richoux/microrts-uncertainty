#!/bin/bash
javac -cp src/:lib/jdom.jar:lib/minimal-json-0.9.4.jar src/tests/POGameVisualSimulationTest.java

java -cp src:lib/jdom.jar:minimal-json-0.9.4.jar tests.POGameVisualSimulationTest
