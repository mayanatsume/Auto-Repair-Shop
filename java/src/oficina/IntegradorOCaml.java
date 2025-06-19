package oficina;

import java.io.*;
import java.util.*;

public class IntegradorOCaml {
    /** 
     * Executa ./main.exe <comando> [args] dentro de ../ocaml,
     * retorna as linhas de saída, ou lança IOException em caso de erro.
     */
    public static List<String> run(String comando, String args) throws IOException {
        // Monta o comando
        List<String> cmd = new ArrayList<>();
        cmd.add("./main.exe");
        cmd.add(comando);
        if (args != null && !args.isBlank()) {
            cmd.add(args.trim());
        }

        // Prepara e executa
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File("../ocaml"));
        Process p = pb.start();

        // Espera terminar
        try {
            int code = p.waitFor();
            if (code != 0) {
                // Captura stderr e lança erro com a mensagem do OCaml
                BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = err.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                throw new IOException("OCaml retornou código " + code + ": " + sb.toString().trim());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Execução interrompida", e);
        }

        // Lê stdout
        BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
        List<String> linhas = new ArrayList<>();
        String l;
        while ((l = out.readLine()) != null) {
            linhas.add(l);
        }
        return linhas;
    }
}