(* table_utils.ml *)

(* Utilitário para imprimir tabelas alinhadas no terminal *)

let pad s w =
  let len = String.length s in
  if len >= w then s
  else s ^ String.make (w - len) ' '

let print_table headers rows =
  (* Determina largura de cada coluna *)
  let num_cols = List.length headers in
  let cols_width =
    List.init num_cols (fun i ->
      let header_len = String.length (List.nth headers i) in
      let max_cell =
        List.fold_left (fun acc row ->
          max acc (String.length (List.nth row i))
        ) header_len rows
      in
      max_cell
    )
  in
  (* Imprime cabeçalho *)
  List.iteri (fun i h ->
    let w = List.nth cols_width i in
    Printf.printf "%s  " (pad h w)
  ) headers;
  print_endline "";
  (* Imprime linha separadora *)
  List.iter (fun w ->
    Printf.printf "%s  " (String.make w '-')
  ) cols_width;
  print_endline "";
  (* Imprime linhas de dados *)
  List.iter (fun row ->
    List.iteri (fun i cell ->
      let w = List.nth cols_width i in
      Printf.printf "%s  " (pad cell w)
    ) row;
    print_endline ""
  ) rows

(* Exemplo de uso: *)
(* let headers = ["ID"; "Nome"; "Marca"] in *)
(* let rows = [["1";"Oleo";"Shell"]; ["2";"Vela";"Bosh"]] in *)
(* print_table headers rows *) *)
