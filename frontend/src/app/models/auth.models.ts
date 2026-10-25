export type PerfilInvestidor = "CONSERVADOR" | "MODERADO" | "ARROJADO";

export interface AuthUsuario {
  id?: string;
  nome?: string;
  email?: string;
  perfilInvestidor?: PerfilInvestidor;
  cpf?: string;
  telefone?: string;
  dataNascimento?: string;
  banco?: string;
  agencia?: string;
  numeroConta?: string;
  permissoes?: string[];
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken?: string;
  token?: string;
  tipo?: "Bearer";
  usuario?: UsuarioResponse;
  expiresIn?: number;
}

export interface RegistroRequest {
  nome: string;
  email: string;
  senha: string;
  cpf: string;
  telefone: string;
  dataNascimento: string;
  banco: string;
  agencia: string;
  numeroConta: string;
  perfilInvestidor: PerfilInvestidor;
}

export interface AtualizarUsuarioRequest {
  nome: string;
  telefone: string;
  dataNascimento: string;
  perfilInvestidor: PerfilInvestidor;
}

export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
  perfilInvestidor: PerfilInvestidor;
  cpf: string;
  telefone: string;
  dataNascimento: string;
  banco: string;
  agencia: string;
  numeroConta: string;
  permissoes: string[];
}
