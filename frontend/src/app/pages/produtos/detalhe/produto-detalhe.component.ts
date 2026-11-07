import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { ProdutoService } from "../../../services/produto.service";
import type { Indexador, ProdutoFinanceiro, TipoLiquidez, TipoProduto } from "../../../models/produto.models";

@Component({
  selector: "app-produto-detalhe",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./produto-detalhe.component.html",
  styleUrl: "./produto-detalhe.component.scss",
})
export class ProdutoDetalheComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly produtoService = inject(ProdutoService);

  readonly produto = signal<ProdutoFinanceiro | null>(null);
  readonly relacionados = signal<ProdutoFinanceiro[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get("id");
      this.carregando.set(true);
      this.erro.set(null);
      this.produto.set(null);
      this.relacionados.set([]);

      if (!id) {
        this.carregando.set(false);
        this.erro.set("Produto não informado.");
        return;
      }

      this.produtoService
        .buscarPorId(id)
        .pipe(finalize(() => this.carregando.set(false)))
        .subscribe({
          next: (produto) => {
            this.produto.set(produto);
            this.produtoService.buscarRelacionados(id).subscribe({
              next: (relacionados) => this.relacionados.set(relacionados ?? []),
              error: () => this.relacionados.set([]),
            });
          },
          error: (err) => {
            this.erro.set(String(err?.error?.erro ?? "Não foi possível carregar o produto."));
          },
        });
      });
  }

  formatarTipo(tipo: TipoProduto): string {
    return {
      CDB: "CDB",
      LCI: "LCI",
      LCA: "LCA",
      LC: "LC",
      TESOURO_SELIC: "Tesouro Selic",
      POUPANCA: "Poupança",
      FUNDO: "Fundo",
      ACAO: "Ação",
    }[tipo] ?? tipo;
  }

  formatarRisco(risco: string): string {
    return { BAIXO: "Baixo", MEDIO: "Médio", ALTO: "Alto" }[risco] ?? risco;
  }

  formatarLiquidez(liquidez: TipoLiquidez, dias: number | null): string {
    if (liquidez === "DIARIA") return "Diária";
    if (liquidez === "D_PLUS_N") return `D+${dias ?? "N"}`;
    return "No vencimento";
  }

  formatarPrazo(prazo: number | null): string {
    return prazo && prazo > 0 ? `${prazo} dias` : "Livre";
  }

  formatarCarencia(carencia: number | null): string {
    return carencia && carencia > 0 ? `${carencia} dias` : "Sem carência";
  }

  formatarMoeda(valor: number): string {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarPercentual(valor: number | null): string {
    return valor === null || valor === undefined ? "Não informado" : `${valor.toFixed(2).replace(".", ",")}%`;
  }

  formatarIndexador(indexador: Indexador | null): string {
    if (!indexador) {
      return "Não informado";
    }

    return {
      CDI: "CDI",
      SELIC: "Selic",
      IPCA: "IPCA",
      PREFIXADO: "Prefixado",
      IBOVESPA: "Ibovespa",
    }[indexador];
  }

  formatarRegraRentabilidade(produto: ProdutoFinanceiro): string {
    if (produto.percentualIndexador !== null && produto.indexador) {
      return `${this.formatarPercentual(produto.percentualIndexador)} do ${this.formatarIndexador(produto.indexador)}`;
    }
    if (produto.taxaPrefixada !== null) {
      return `${this.formatarPercentual(produto.taxaPrefixada)} ao ano`;
    }
    return "Conforme condições do produto";
  }

  getTipoClass(tipo: TipoProduto): string {
    return `tipo-${tipo.toLowerCase().replace("_", "-")}`;
  }

  getRiscoClass(risco: string): string {
    return `risk-${risco.toLowerCase()}`;
  }
}
