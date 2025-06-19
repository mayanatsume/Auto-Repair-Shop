# Makefile – minimal OCaml build

OCAMLC=ocamlc
STR   = str.cma
EXE   = main.exe

.PHONY: all clean

all: $(EXE)

calculo_maoobra.cmo: calculo_maoobra.ml
	$(OCAMLC) -c calculo_maoobra.ml

calculo_desconto.cmo: calculo_desconto.ml
	$(OCAMLC) -c calculo_desconto.ml

main.cmo: main.ml
	$(OCAMLC) -c $(STR) main.ml

$(EXE): calculo_maoobra.cmo calculo_desconto.cmo main.cmo
	$(OCAMLC) -o $(EXE) $(STR) calculo_maoobra.cmo calculo_desconto.cmo main.cmo

clean:
	rm -f *.cmo *.cmi $(EXE)
