MAIN = README
IMAGES = images/*

all: $(MAIN)

$(MAIN): %: %.tex $(IMAGES)
	pdflatex $<

clean:
	-rm -f *.{log,aux,bcf,run.xml,out}

.PHONY: clean all

