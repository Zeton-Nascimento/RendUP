import { CommonModule } from "@angular/common";
import { Component, input, output, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";

@Component({
  selector: "app-depositar-modal",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./depositar-modal.component.html",
  styleUrl: "./depositar-modal.component.scss",
})
export class DepositarModalComponent {
  readonly carregando = input(false);
  readonly erro = input<string | null>(null);
  readonly fechado = output<void>();
  readonly confirmado = output<number>();

  readonly valor = signal<number | null>(null);

  fechar(): void {
    if (this.carregando()) return;
    this.fechado.emit();
  }

  confirmar(): void {
    const valor = this.valor();
    if (valor === null || Number.isNaN(valor) || valor <= 0 || this.carregando()) return;
    this.confirmado.emit(valor);
  }
}
