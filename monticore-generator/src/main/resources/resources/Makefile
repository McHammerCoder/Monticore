JSOUJSOURCES := Hammer.java
JSOURCES_NATIVE := Hammer
CSOURCES := com_upstandinghackers_hammer_Hammer.c

# ls *.h *.o *.so com/upstandinghackers/hammer/*.class | grep -v jhammer.h | tr '\n' ' '; replace single $ with $$
OUTPUTS := com/upstandinghackers/hammer/Hammer.class com_upstandinghackers_hammer_Hammer.h com_upstandinghackers_hammer_Hammer.o libjhammer_actions.so

TOPLEVEL := .

JC=javac
JH=javah
CP=com/upstandinghackers/hammer
PACKAGE=com.upstandinghackers.hammer

include ./common.mk


CFLAGS += -fPIC -L $(TOPLEVEL) -ljhammer -I. -I $(TOPLEVEL)/src/ -I $(TOPLEVEL)/include/ -I $(TOPLEVEL)/lib/ -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux

%.java: $(call ifsilent,| $(HUSH))
	$(JC) $(CP)/$@

all: javacc prepare compile link

link: compile
	$(CC) -shared $(CFLAGS) -o libjhammer_actions.so *.o libjhammer.so

$(CSOURCES): prepare
	$(CC) -c $(CFLAGS) $@

compile: prepare $(CSOURCES)

prepare: javacc $(JSOURCES_NATIVE)

$(JSOURCES_NATIVE): javacc
	$(JH) $(PACKAGE).$@

javacc: $(JSOURCES)

#TODO make this not-as-hardcoded
#clean:
#	rm $(CP)/*.class && rm com_upstandinghackers_*.h && rm com_upstandinghackers_*.o && rm libjhammer.so