#!/bin/bash

g++ constraints_rts.cpp obj_rts.cpp main.cpp -Ofast -DBENCH -W -Wall -Wextra -pedantic -Wno-sign-compare -Wno-unused-parameter -o ../solver_cpp -lghost 
