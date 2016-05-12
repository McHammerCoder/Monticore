JSOUJSOURCES := default
JSOURCES_NATIVE := default
CSOURCES := default

TOPLEVEL := ./resources

JC=javac
JH=javah
CP=com/upstandinghackers/hammer
PACKAGE=com.upstandinghackers.hammer


CFLAGS += -fPIC -L $(TOPLEVEL) -ljhammer -I. -I $(TOPLEVEL)/include -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux

%.java: $(call ifsilent,| $(HUSH))
	$(JC) $(CP)/$@

all: javacc prepare compile link

link: compile
	$(CC) -shared $(CFLAGS) -o $(subst .c,.so,$(CSOURCES)) $(subst .c,.o,$(CSOURCES)) $(TOPLEVEL)/build/opt/src/*.os $(TOPLEVEL)/build/opt/src/backends/*.os $(TOPLEVEL)/build/opt/src/parsers/*.os

$(CSOURCES): prepare
	$(CC) -c $(CFLAGS) $@

compile: prepare $(CSOURCES)

prepare: javacc $(subst libjhammer_,,$(subst .c,,$(CSOURCES)))Hammer.java

$(subst libjhammer_,,$(subst .c,,$(CSOURCES)))Hammer: javacc
	$(JH) $(PACKAGE).$@

javacc: $(subst libjhammer_,,$(subst .c,,$(CSOURCES)))Hammer

allfiles:
	for dir in $(wildcard *.c); do \
		$(MAKE) all CSOURCES="$$dir"; \
	done
