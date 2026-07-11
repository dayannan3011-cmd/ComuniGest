import { Injectable } from '@angular/core';
import { ApiResourceService } from './api-resource.service';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'perfiles';
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'usuarios';
}

@Injectable({ providedIn: 'root' })
export class DepartamentoService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'departamentos';
}

@Injectable({ providedIn: 'root' })
export class ResidenteService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'residentes';
}

@Injectable({ providedIn: 'root' })
export class TurnoService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'turnos';
}

@Injectable({ providedIn: 'root' })
export class VisitaService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'visitas';
}

@Injectable({ providedIn: 'root' })
export class EncomiendaService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'encomiendas';
}

@Injectable({ providedIn: 'root' })
export class IncidenciaService {
  constructor(public readonly api: ApiResourceService) {}
  readonly resource = 'incidencias';
}
