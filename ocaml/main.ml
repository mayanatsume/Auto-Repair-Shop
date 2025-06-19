(* ---------- main.ml ---------- *)
open Scanf
open Str
open Calculo_maoobra
open Calculo_desconto

(* ---------- Types ---------- *)
type item = {
  id    : int;
  nome  : string;
  marca : string;
  cat   : string;
  custo : float;
  preco : float;
}

type service = {
  sid        : int;
  sname      : string;
  cats       : string list;
  n_mec      : int;
  tempo      : float;      (* horas por mecânico *)
  preco_base : float;
}

type discount_brand = {
  brand : string;
  disc  : float;
}

type mechanic = {
  mid        : int;
  mname      : string;
  custo_hora : float;
}

(* ---------- I/O ---------- *)
let read_file file =
  let ic = open_in file in
  let rec loop acc =
    match input_line ic with
    | line -> loop (line :: acc)
    | exception End_of_file -> close_in ic; List.rev acc
  in loop []

let profit i = i.preco -. i.custo

(* ---------- Parsers ---------- *)
let parse_item line =
  try Scanf.sscanf line
      "item(%d, '%[^']', '%[^']', '%[^']', %f, %f, %d)."
      (fun id nome marca cat custo preco _qty -> Some { id; nome; marca; cat; custo; preco })
  with _ -> None

let parse_service line =
  try Scanf.sscanf line
      "servico(%d, '%[^']', [%[^]]], %d, %f, %f)."
      (fun sid sname cats_raw n_mec tempo preco_base ->
         let cats = cats_raw
                    |> Str.split (Str.regexp ", *")
                    |> List.map (fun s ->
                         let s' = String.trim s in
                         let len = String.length s' in
                         if len >= 2 && s'.[0] = '\'' && s'.[len-1] = '\'' then
                           String.sub s' 1 (len-2)
                         else s')
         in Some { sid; sname; cats; n_mec; tempo; preco_base })
  with _ -> None

let parse_discount_marca line =
  try Scanf.sscanf line
      "desconto_marca('%[^']', %f)."
      (fun brand disc -> Some { brand; disc })
  with _ -> None

let parse_mechanic line =
  try Scanf.sscanf line
      "mecanico(%d, %S, %f)."
      (fun mid mname custo_hora -> Some { mid; mname; custo_hora })
  with _ -> None

(* ---------- Utility ---------- *)
let rec take_n lst n = if n <= 0 then [] else match lst with x::xs -> x :: take_n xs (n-1) | [] -> []

(* ---------- Commands ---------- *)
let listar_items () =
  let items = read_file "../database/database.pl" |> List.filter_map parse_item
              |> List.sort (fun a b -> match compare a.cat b.cat with 0 -> (match compare a.marca b.marca with 0 -> compare a.nome b.nome | d -> d) | d -> d)
  in Printf.printf "ID;Nome;Marca;Tipo;Custo;Preco\n";
     List.iter (fun i -> Printf.printf "%d;%s;%s;%s;%.2f;%.2f\n" i.id i.nome i.marca i.cat i.custo i.preco) items

let top_n n =
  let sorted = read_file "../database/database.pl" |> List.filter_map parse_item
                |> List.sort (fun a b -> compare (profit b) (profit a)) in
  let topn = take_n sorted n in
  Printf.printf "ID;Nome;Marca;Tipo;Lucro\n";
  List.iter (fun i -> Printf.printf "%d;%s;%s;%s;%.2f\n" i.id i.nome i.marca i.cat (profit i)) topn

let listar_servicos () =
  let svcs = read_file "../database/database.pl" |> List.filter_map parse_service in
  Printf.printf "ID;Nome;PrecoBase\n";
  List.iter (fun s -> Printf.printf "%d;%s;%.2f\n" s.sid s.sname s.preco_base) svcs

(** Lista todos os itens mostrando ID;Categoria;Nome;Marca;Preco;Desconto%;Lucro **)
let listar_descontos () =
  let itens = read_file "../database/database.pl" |> List.filter_map parse_item in
  (* Cabeçalho *)
  Printf.printf "ID;Categoria;Nome;Marca;Preco;%%Desconto;Lucro\n";
  List.iter (fun i ->
    let pct       = get_discount_pct i.marca in
    let valor_desc= valor_desconto i.preco pct in
    let preco_fin = preco_final i.preco valor_desc in
    let lucro     = preco_fin -. i.custo in
    let pct_int   = int_of_float (pct *. 100.0) in
    Printf.printf "%d;%s;%s;%s;%.2f;%d%%;%.2f\n"
      i.id i.cat i.nome i.marca i.preco pct_int lucro
  ) itens

let orcamento_items ids_str =
  let ids   = Str.split (Str.regexp ", *") ids_str |> List.map int_of_string in
  let svcs  = read_file "../database/database.pl" |> List.filter_map parse_service in
  let itens = read_file "../database/database.pl" |> List.filter_map parse_item in
  (* Cabeçalho ajustado *)
  Printf.printf "ServID;ServNome;Categoria;ItemNome;Marca;Lucro\n";
  List.iter (fun id ->
    match List.find_opt (fun s -> s.sid = id) svcs with
    | None -> ()
    | Some srv ->
      List.iter (fun cat ->
        let bycat = List.filter (fun i -> i.cat = cat) itens in
        match bycat with
        | [] -> ()
        | hd::_ ->
          let best = List.fold_left (fun acc i ->
                          if profit i > profit acc then i else acc
                        ) hd bycat in
          (* ServID;ServNome;Categoria;ItemNome;Marca;Lucro *)
          Printf.printf "%d;%s;%s;%s;%s;%.2f\n"
            srv.sid
            srv.sname
            cat
            best.nome
            best.marca
            (profit best)
      ) srv.cats
  ) ids

let orcamento_mecanico ids_str =
  let ids       = Str.split (Str.regexp ", *") ids_str |> List.map int_of_string in
  let svcs      = read_file "../database/database.pl" |> List.filter_map parse_service in
  let mecs      = read_file "../database/database.pl" |> List.filter_map parse_mechanic in
  let avg_custo =
    List.fold_left (fun acc m -> acc +. m.custo_hora) 0.0 mecs
    /. float_of_int (List.length mecs)
  in
  (* Novo cabeçalho: inclui ServNome e CustoSemDesconto *)
  Printf.printf "ServID;ServNome;Horas;CustoHora;CustoSemDesconto;DescontoAplicado;Total\n";
  List.iter (fun id ->
    match List.find_opt (fun s -> s.sid = id) svcs with
    | None -> ()
    | Some srv ->
      let horas      = srv.tempo *. float_of_int srv.n_mec in
      let custo_hora = avg_custo in
      let sem_desc   = custo_sem_desc srv.tempo custo_hora srv.n_mec in
      let desconto   = taxa_desconto srv.tempo in
      let total      = custo_mao_obra srv.tempo custo_hora srv.n_mec in
      (* Agora incluímos srv.sname e sem_desc no lugar certo *)
      Printf.printf "%d;%s;%.2f;%.2f;%.2f;%.2f;%.2f\n"
        srv.sid
        srv.sname
        horas
        custo_hora
        sem_desc
        desconto
        total
  ) ids

let orcamento_desconto_items ids_str =
  let ids = Str.split (Str.regexp ", *") ids_str |> List.map int_of_string in
  let svcs = read_file "../database/database.pl" |> List.filter_map parse_service in
  let itens = read_file "../database/database.pl" |> List.filter_map parse_item in
  Printf.printf "ID;Nome;Marca;Custo;Preco;%%Desconto;ValorDesconto;PrecoFinal\n";
  List.iter (fun id -> match List.find_opt (fun s -> s.sid = id) svcs with
    | None -> ()
    | Some srv -> List.iter (fun cat -> let bycat = List.filter (fun i -> i.cat = cat) itens in match bycat with
        | [] -> ()
        | hd::_ -> let best = List.fold_left (fun acc i -> if profit i > profit acc then i else acc) hd bycat in
                    let pct        = get_discount_pct best.marca in
                    let valor_desc = valor_desconto best.preco pct in
                    let preco_fin  = preco_final best.preco valor_desc in
                    let pct_int    = int_of_float (pct *. 100.0) in
                    Printf.printf "%d;%s;%s;%.2f;%.2f;%d%%;%.2f;%.2f\n"
                      best.id
                      best.nome
                      best.marca
                      best.custo
                      best.preco
                      pct_int
                      valor_desc
                      preco_fin
        ) srv.cats
  ) ids

let orcamento_preco_fixo ids_str =
  let ids = Str.split (Str.regexp ", *") ids_str |> List.map int_of_string in
  let svcs = read_file "../database/database.pl" |> List.filter_map parse_service in
  Printf.printf "ID;Nome;PrecoFixo\n";
  List.iter (fun id -> match List.find_opt (fun s -> s.sid = id) svcs with
    | None -> ()
    | Some srv when srv.preco_base > 0.0 -> Printf.printf "%d;%s;%.2f\n" srv.sid srv.sname srv.preco_base
    | _ -> ()) ids

let usage () =
  Printf.printf "Uso:\n\
  listar_items\n\
  top_n <n>\n\
  listar_servicos\n\
  orcamento_items <ids>\n\
  orcamento_mecanico <ids>\n\
  orcamento_desconto_items <ids>\n\
  orcamento_preco_fixo <ids>\n\
  listar_descontos\n";
  exit 1

let () = match Sys.argv with
  | [| _; "listar_items" |] -> listar_items ()
  | [| _; "top_n"; nstr |] -> top_n (int_of_string nstr)
  | [| _; "listar_servicos" |] -> listar_servicos ()
  | [| _; "orcamento_items"; ids |] -> orcamento_items ids
  | [| _; "orcamento_mecanico"; ids |] -> orcamento_mecanico ids
  | [| _; "orcamento_desconto_items"; ids |] -> orcamento_desconto_items ids
  | [| _; "orcamento_preco_fixo"; ids |] -> orcamento_preco_fixo ids
  | [| _; "listar_descontos" |] -> listar_descontos ()
  | _ -> usage ()