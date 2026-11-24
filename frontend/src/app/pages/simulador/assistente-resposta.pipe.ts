import { Pipe, PipeTransform, inject } from "@angular/core";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";

@Pipe({ name: "assistenteResposta", standalone: true })
export class AssistenteRespostaPipe implements PipeTransform {
  private readonly sanitizer = inject(DomSanitizer);

  transform(texto: string | null | undefined): SafeHtml {
    if (!texto) return "";

    const linhas = texto.split(/\r?\n/);
    const html: string[] = [];
    let indice = 0;

    while (indice < linhas.length) {
      const linha = linhas[indice].trim();
      const proxima = linhas[indice + 1]?.trim() ?? "";

      if (linha.includes("|") && /^\|?\s*:?-{3,}/.test(proxima)) {
        const cabecalho = this.celulas(linha);
        const linhasTabela: string[][] = [];
        indice += 2;

        while (indice < linhas.length && linhas[indice].includes("|")) {
          linhasTabela.push(this.celulas(linhas[indice].trim()));
          indice += 1;
        }

        html.push("<div class=\"resposta-tabela-wrapper\"><table class=\"resposta-tabela\"><thead><tr>");
        html.push(cabecalho.map((celula) => `<th>${this.inline(celula)}</th>`).join(""));
        html.push("</tr></thead><tbody>");
        for (const linhaTabela of linhasTabela) {
          html.push("<tr>");
          html.push(linhaTabela.map((celula) => `<td>${this.inline(celula)}</td>`).join(""));
          html.push("</tr>");
        }
        html.push("</tbody></table></div>");
        continue;
      }

      if (linha === "") {
        indice += 1;
        continue;
      }

      if (/^[-_]{2,}$/.test(linha)) {
        indice += 1;
        continue;
      }

      const titulo = /^(#{1,3})\s+(.+)$/.exec(linha);
      if (titulo) {
        html.push(`<h4>${this.inline(titulo[2])}</h4>`);
        indice += 1;
        continue;
      }

      const lista = /^(?:[-*]|\d+\.)\s+(.+)$/.exec(linha);
      if (lista) {
        const itens: string[] = [];
        while (indice < linhas.length) {
          const item = /^(?:[-*]|\d+\.)\s+(.+)$/.exec(linhas[indice].trim());
          if (!item) break;
          itens.push(`<li>${this.inline(item[1])}</li>`);
          indice += 1;
        }
        html.push(`<ul>${itens.join("")}</ul>`);
        continue;
      }

      html.push(`<p>${this.inline(linha)}</p>`);
      indice += 1;
    }

    return this.sanitizer.bypassSecurityTrustHtml(html.join(""));
  }

  private celulas(linha: string): string[] {
    return linha
      .replace(/^\|/, "")
      .replace(/\|$/, "")
      .split("|")
      .map((celula) => celula.trim());
  }

  private inline(texto: string): string {
    return this.escape(texto)
      .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
      .replace(/\*(.+?)\*/g, "<em>$1</em>");
  }

  private escape(texto: string): string {
    return texto
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }
}
