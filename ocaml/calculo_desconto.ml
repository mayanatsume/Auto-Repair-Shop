(* ---------- calculo_desconto.ml ---------- *)

(** Retorna a porcentagem de desconto para uma marca *)
let get_discount_pct brand =
  match brand with
  | "Michelin" -> 0.10
  | "Bosch"    -> 0.15
  | "Philips"  -> 0.30
  | _          -> 0.0

(** Dado preço e pct, retorna o valor absoluto do desconto *)
let valor_desconto preco pct =
  preco *. pct

(** Subtrai o desconto do preço original *)
let preco_final preco desconto =
  preco -. desconto
