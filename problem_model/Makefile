EXEC_PESSIMISTIC=solver_cpp_pessimistic
EXEC_OPTIMISTIC=solver_cpp_optimistic

# Compiler flags
CXXFIRSTFLAGS= -Ofast -DBENCH -W -Wall -Wextra -pedantic -Wno-sign-compare -Wno-unused-parameter
CXXFIRSTFLAGSDEBUG= -g -O0 -DBENCH -W -Wall -Wextra -pedantic -Wno-sign-compare -Wno-unused-parameter 

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	CXX=g++
	CXXFLAGS= -std=c++14 $(CXXFIRSTFLAGS)
	CXXFLAGSDEBUG= -std=c++14 $(CXXFIRSTFLAGSDEBUG)
endif
ifeq ($(UNAME_S),Darwin)
	CXX=clang++
	CXXFLAGS= -std=c++1y  -stdlib=libc++ $(CXXFIRSTFLAGS)
	CXXFLAGSDEBUG= -std=c++1y  -stdlib=libc++ $(CXXFIRSTFLAGSDEBUG)
	LDFLAGS=-pthread -lghost -lc++ -lc++abi
endif

# Directories
SRCDIR=src
OBJDIR=obj
#BINDIR=../src/ai/poadaptive
BINDIR=.
INCLUDEDIR=../include

# Linker flags
LDFLAGS= -pthread -lghost

# Files
#SOURCES=$(foreach sdir, $(SRCDIR), $(wildcard $(sdir)/*.cpp))
#OBJECTS=$(patsubst %.cpp, $(OBJDIR)/%.o, $(notdir $(SOURCES)))

# For rm
SOURCESTILDE=$(foreach sdir, $(SRCDIR), $(wildcard $(sdir)/*.cpp~))

vpath %.cpp $(SRCDIR)

# Reminder, 'cause it is easy to forget makefile's fucked-up syntax...
# $@ is what triggered the rule, ie the target before :
# $^ is the whole dependencies list, ie everything after :
# $< is the first item in the dependencies list

# Rules
all: $(BINDIR)/$(EXEC_PESSIMISTIC) $(BINDIR)/$(EXEC_OPTIMISTIC) 

debug: CXXFLAGS= $(CXXFLAGSDEBUG)
debug: $(BINDIR)/$(EXEC)

$(BINDIR)/$(EXEC_PESSIMISTIC): $(OBJDIR)/constraints_rts.o $(OBJDIR)/obj_rts.o $(OBJDIR)/main_pessimistic.o
	$(CXX) -o  $@ $^ $(LDFLAGS)
	ln -s ../../../problem_model/$(EXEC_PESSIMISTIC) ../src/ai/poadaptive/

$(BINDIR)/$(EXEC_OPTIMISTIC): $(OBJDIR)/constraints_rts.o $(OBJDIR)/obj_rts.o $(OBJDIR)/main_optimistic.o
	$(CXX) -o $@ $^ $(LDFLAGS)
	ln -s ../../../problem_model/$(EXEC_OPTIMISTIC) ../src/ai/poadaptive/

$(OBJDIR)/main_pessimistic.o: $(SRCDIR)/main.cpp
	$(CXX) $(CXXFLAGS) -c -DPESSIMISTIC -I$(INCLUDEDIR) $< -o $@

$(OBJDIR)/main_optimistic.o: $(SRCDIR)/main.cpp
	$(CXX) $(CXXFLAGS) -c -DOPTIMISTIC -I$(INCLUDEDIR) $< -o $@

$(OBJDIR)/%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c -I$(INCLUDEDIR) $< -o $@

.PHONY: clean 

clean:
	rm -fr core *~ $(EXEC_PESSIMISTIC) $(EXEC_OPTIMISTIC) $(OBJDIR)/*.o $(SOURCESTILDE) 
