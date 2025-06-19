(* ---------- calculo_maoobra.ml ---------- *)

(** Custo sem desconto: tempo (h) × custo por hora × nº de mecânicos *)
let custo_sem_desc tempo custo_hora n_mec =
  tempo *. custo_hora *. float_of_int n_mec

(** Percentual de desconto com base no tempo *)
let taxa_desconto tempo =
  if tempo < 0.25 then 0.05
  else if tempo > 4.0 then 0.15
  else 0.0

(** Custo final após aplicar desconto *)
let custo_mao_obra tempo custo_hora n_mec =
  let sem = custo_sem_desc tempo custo_hora n_mec in
  let d   = taxa_desconto tempo in
  sem *. (1.0 -. d)
