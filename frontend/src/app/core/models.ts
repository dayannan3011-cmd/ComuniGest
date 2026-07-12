export interface Perfil {
  id?: number;
  nombre: string;
  descripcion?: string;
  activo?: boolean;
}

export interface Usuario {
  id?: number;
  nombre: string;
  email: string;
  password?: string;
  perfil?: Perfil | IdRef;
  activo?: boolean;
}

export interface IdRef {
  id: number;
}

export interface Departamento {
  id?: number;
  torre: string;
  numero: string;
  piso?: number;
  estado?: string;
  observaciones?: string;
  creadoEn?: string;
  actualizadoEn?: string;
}

export interface Residente {
  id?: number;
  nombres: string;
  apellidos: string;
  rut?: string;
  telefono?: string;
  email?: string;
  tipoResidente?: string;
  departamento?: Departamento | IdRef;
  activo?: boolean;
  creadoEn?: string;
  actualizadoEn?: string;
}

export interface ReporteResumen {
  residentesActivos: number;
  turnosAbiertos: number;
  visitasDentro: number;
  encomiendasPendientes: number;
  incidenciasAbiertas: number;
}

export interface LoginResponse {
  id: number;
  nombre: string;
  email: string;
  perfil: string;
  token: string;
}
