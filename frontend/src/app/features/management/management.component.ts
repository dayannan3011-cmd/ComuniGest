import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, ValidatorFn, Validators } from '@angular/forms';
import { DatePipe, JsonPipe } from '@angular/common';
import { Observable, switchMap } from 'rxjs';
import { ApiResourceService } from '../../core/api-resource.service';
import { AuthService } from '../../core/auth.service';
import { ReporteResumen } from '../../core/models';

type FieldType = 'text' | 'number' | 'email' | 'password' | 'textarea' | 'checkbox' | 'select';

interface FieldConfig {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
  maxLength?: number;
  min?: number;
  max?: number;
  initialValue?: unknown;
  options?: SelectOption[];
}

interface SelectOption {
  value: number | string;
  label: string;
}

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [ReactiveFormsModule, JsonPipe, DatePipe],
  template: `
    <main class="content">
      <div class="content-header">
        <div>
          <p class="eyebrow">Modulo</p>
          <h1>{{ title }}</h1>
        </div>
        <button type="button" class="secondary-button" (click)="refresh()" [disabled]="refreshing">
          {{ refreshing ? 'Actualizando...' : 'Actualizar' }}
        </button>
      </div>

      @if (resource === 'reportes') {
        <section class="metrics-grid">
          @for (metric of reportEntries; track metric.key) {
            <article class="metric">
              <span>{{ metric.label }}</span>
              <strong>{{ metric.value }}</strong>
            </article>
          }
        </section>
      } @else {
        <section class="editor-layout"
          [class.encomiendas-consulta]="resource === 'encomiendas' && !isConserje"
          [class.incidencias-gestion]="resource === 'incidencias' && !isConserje">
          @if (resource === 'turnos') {
            <section class="panel form-grid" [formGroup]="form">
              <h2>Control de turno</h2>
              <p><strong>Conserje conectado:</strong> {{ currentUserName }}</p>

              @if (isConserje) {
                @if (turnoEnCurso) {
                  <p><strong>Turno en curso desde {{ $any(turnoEnCurso['fechaInicio']) | date:'dd/MM/yyyy HH:mm' }}</strong></p>
                  <button type="button" [disabled]="loading" (click)="cerrarTurno()">
                    {{ loading ? 'Cerrando...' : 'Cerrar turno' }}
                  </button>
                } @else {
                  <button type="button" [disabled]="loading" (click)="iniciarTurno()">
                    {{ loading ? 'Iniciando...' : 'Iniciar turno' }}
                  </button>
                }
              } @else {
                <p>Consulta general de turnos. El Administrador no inicia turnos desde este módulo.</p>
              }

              @if (error) {
                <p class="form-error">{{ error }}</p>
              }
              @if (success) {
                <p>{{ success }}</p>
              }
            </section>
          } @else if (resource === 'visitas') {
            <form [formGroup]="form" (ngSubmit)="registrarVisita()" class="panel form-grid">
              <h2>{{ isConserje ? 'Registrar ingreso de visita' : 'Consulta de visitas' }}</h2>
              @if (isConserje) {
                <label>
                  Nombre completo del visitante
                  <input type="text" formControlName="nombreVisitante" maxlength="160">
                </label>
                <label>
                  Documento o RUT
                  <input type="text" formControlName="documento" maxlength="40">
                </label>
                <label>
                  Patente
                  <input type="text" formControlName="patente" maxlength="20">
                </label>
                <label>
                  Departamento
                  <div role="combobox" aria-haspopup="listbox" [attr.aria-expanded]="departmentSuggestionsVisible">
                    <input
                      type="search"
                      placeholder="Buscar por torre o número"
                      autocomplete="off"
                      [value]="departmentSearch"
                      (focus)="showDepartmentSuggestions()"
                      (input)="onDepartmentSearch($any($event.target).value)">
                    @if (departmentSuggestionsVisible) {
                      <div role="listbox">
                        @for (option of filteredDepartmentOptions; track option.value) {
                          <button
                            type="button"
                            role="option"
                            (mousedown)="$event.preventDefault()"
                            (click)="selectDepartment(option)">
                            {{ option.label }}
                          </button>
                        } @empty {
                          <p>No se encontraron departamentos</p>
                        }
                      </div>
                    }
                  </div>
                </label>
                <button type="submit" [disabled]="form.invalid || loading">
                  {{ loading ? 'Registrando...' : 'Registrar visita' }}
                </button>
              } @else {
                <p>Consulta general de visitas. El Administrador no registra ingresos ni salidas.</p>
              }

              @if (error) {
                <p class="form-error">{{ error }}</p>
              }
              @if (success) {
                <p>{{ success }}</p>
              }
            </form>
          } @else if (resource === 'encomiendas') {
            @if (isConserje) {
            <form [formGroup]="form" (ngSubmit)="registrarEncomienda()" class="panel form-grid">
              <h2>{{ isConserje ? 'Registrar encomienda' : 'Consulta de encomiendas' }}</h2>
              @if (isConserje) {
                <label>Destinatario
                  <input type="text" formControlName="destinatario" maxlength="160">
                </label>
                <label>Departamento
                  <div role="combobox" aria-haspopup="listbox" [attr.aria-expanded]="departmentSuggestionsVisible">
                    <input type="search" placeholder="Buscar por torre o número" autocomplete="off"
                      [value]="departmentSearch" (focus)="showDepartmentSuggestions()"
                      (input)="onDepartmentSearch($any($event.target).value)">
                    @if (departmentSuggestionsVisible) {
                      <div role="listbox">
                        @for (option of filteredDepartmentOptions; track option.value) {
                          <button type="button" role="option" (mousedown)="$event.preventDefault()"
                            (click)="selectDepartment(option)">{{ option.label }}</button>
                        } @empty { <p>No se encontraron departamentos</p> }
                      </div>
                    }
                  </div>
                </label>
                <label>Descripción
                  <textarea formControlName="descripcion" maxlength="255" rows="3"
                    placeholder="Ej.: Sobre, caja pequeña, paquete grande o electrodoméstico"></textarea>
                </label>
                <label>Empresa o repartidor
                  <input type="text" formControlName="empresaRepartidor" maxlength="160">
                </label>
                <button type="submit" [disabled]="form.invalid || loading">
                  {{ loading ? 'Registrando...' : 'Registrar encomienda' }}
                </button>
              } @else {
                <p>Historial completo de encomiendas. El Administrador no registra recepciones ni entregas.</p>
              }
              @if (error) { <p class="form-error">{{ error }}</p> }
              @if (success) { <p>{{ success }}</p> }
            </form>
            }
          } @else if (resource === 'incidencias') {
            @if (isConserje) {
              <form [formGroup]="form" (ngSubmit)="registrarIncidencia()" class="panel form-grid">
                <h2>Registrar incidencia</h2>
                <label>Título
                  <input type="text" formControlName="titulo" maxlength="140">
                </label>
                <label>Descripción
                  <textarea formControlName="descripcion" maxlength="1000" rows="4"></textarea>
                </label>
                <label>Categoría
                  <select formControlName="categoria">
                    <option value="">Seleccione una categoría</option>
                    @for (option of optionsFor(fields[2]); track option.value) {
                      <option [value]="option.value">{{ option.label }}</option>
                    }
                  </select>
                </label>
                <label>Criticidad
                  <select formControlName="criticidad">
                    <option value="">Seleccione una criticidad</option>
                    @for (option of optionsFor(fields[3]); track option.value) {
                      <option [value]="option.value">{{ option.label }}</option>
                    }
                  </select>
                </label>
                <button type="submit" [disabled]="form.invalid || loading">
                  {{ loading ? 'Registrando...' : 'Registrar incidencia' }}
                </button>
                @if (error) { <p class="form-error">{{ error }}</p> }
                @if (success) { <p>{{ success }}</p> }
              </form>
            }
          } @else {
          <form [formGroup]="form" (ngSubmit)="save()" class="panel form-grid">
            <h2>{{ editingId ? 'Editar registro' : 'Nuevo registro' }}</h2>

            @for (field of fields; track field.key) {
              <label>
                {{ field.label }}
                @if (field.type === 'textarea') {
                  <textarea [formControlName]="field.key" [attr.maxlength]="field.maxLength ?? null" rows="3"></textarea>
                } @else if (field.type === 'checkbox') {
                  <input type="checkbox" [formControlName]="field.key">
                } @else if (field.key === 'departamentoId') {
                  <div role="combobox" aria-haspopup="listbox" [attr.aria-expanded]="departmentSuggestionsVisible">
                    <input
                      type="search"
                      placeholder="Buscar por torre o numero"
                      autocomplete="off"
                      [value]="departmentSearch"
                      (focus)="showDepartmentSuggestions()"
                      (input)="onDepartmentSearch($any($event.target).value)">
                    @if (departmentSuggestionsVisible) {
                      <div role="listbox">
                        @for (option of filteredDepartmentOptions; track option.value) {
                          <button
                            type="button"
                            role="option"
                            (mousedown)="$event.preventDefault()"
                            (click)="selectDepartment(option)">
                            {{ option.label }}
                          </button>
                        } @empty {
                          <p>No se encontraron departamentos</p>
                        }
                      </div>
                    }
                  </div>
                } @else if (field.type === 'select') {
                  <select [formControlName]="field.key">
                    <option value="">Seleccione una opción</option>
                    @for (option of optionsFor(field); track option.value) {
                      <option [value]="option.value">{{ option.label }}</option>
                    }
                  </select>
                } @else {
                  <input
                    [type]="field.type"
                    [formControlName]="field.key"
                    [attr.maxlength]="field.maxLength ?? null"
                    [attr.min]="field.min ?? null"
                    [attr.max]="field.max ?? null">
                }
              </label>
            }

            @if (error) {
              <p class="form-error">{{ error }}</p>
            }
            @if (success) {
              <p>{{ success }}</p>
            }

            <div class="button-row">
              <button type="submit" [disabled]="form.invalid || loading">
                {{ loading ? 'Guardando...' : 'Guardar' }}
              </button>
              <button type="button" class="secondary-button" (click)="resetForm()">Limpiar</button>
            </div>
          </form>
          }

          <section class="panel table-panel">
            @if (resource !== 'incidencias') { <h2>Registros</h2> }
            @if (resource === 'encomiendas') {
              <label>
                Estado
                <select [value]="encomiendaEstado" (change)="onEncomiendaEstadoChange($any($event.target).value)">
                  <option value="TODAS">Todas</option>
                  <option value="PENDIENTE">Pendientes</option>
                  <option value="ENTREGADA">Entregadas</option>
                </select>
              </label>
            } @else if (resource === 'incidencias') {
              <h2>{{ isConserje ? 'Consulta de incidencias' : 'Gestión de incidencias' }}</h2>
              <div class="incidencia-filtros">
                <label>Estado
                  <select [value]="incidenciaEstado" (change)="incidenciaEstado = $any($event.target).value">
                    <option value="TODAS">Todas</option><option value="ABIERTA">Abiertas</option>
                    <option value="EN_PROCESO">En proceso</option><option value="RESUELTA">Resueltas</option>
                  </select>
                </label>
                <label>Categoría
                  <select [value]="incidenciaCategoria" (change)="incidenciaCategoria = $any($event.target).value">
                    <option value="TODAS">Todas</option><option value="SEGURIDAD">Seguridad</option>
                    <option value="INFRAESTRUCTURA">Infraestructura</option><option value="ACCESO">Acceso</option>
                    <option value="RUIDO O CONVIVENCIA">Ruido o convivencia</option>
                    <option value="SERVICIOS">Servicios</option><option value="OTRO">Otro</option>
                  </select>
                </label>
                <label>Criticidad
                  <select [value]="incidenciaCriticidad" (change)="incidenciaCriticidad = $any($event.target).value">
                    <option value="TODAS">Todas</option><option value="BAJA">Baja</option>
                    <option value="MEDIA">Media</option><option value="ALTA">Alta</option>
                    <option value="CRÍTICA">Crítica</option>
                  </select>
                </label>
              </div>
              @if (!isConserje && error) { <p class="form-error">{{ error }}</p> }
              @if (!isConserje && success) { <p>{{ success }}</p> }
            }
            <div class="table-wrap">
              <table>
                <thead>
                  @if (resource === 'turnos') {
                    <tr>
                      <th>Conserje</th>
                      <th>Fecha y hora de inicio</th>
                      <th>Fecha y hora de cierre</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  } @else if (resource === 'visitas') {
                    <tr>
                      <th>Visitante</th>
                      <th>Documento/RUT</th>
                      <th>Patente</th>
                      <th>Departamento</th>
                      <th>Fecha y hora de ingreso</th>
                      <th>Fecha y hora de salida</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  } @else if (resource === 'encomiendas') {
                    <tr>
                      <th>Destinatario</th><th>Departamento</th><th>Descripción</th>
                      <th>Empresa o repartidor</th><th>Fecha y hora de recepción</th><th>Recibida por</th>
                      <th>Fecha y hora de entrega</th><th>Entregada por</th><th>Entregada a</th>
                      <th>Estado</th><th>Acciones</th>
                    </tr>
                  } @else if (resource === 'incidencias') {
                    <tr>
                      <th>Fecha y hora</th><th>Título</th><th>Descripción</th><th>Categoría</th><th>Criticidad</th>
                      <th>Registrada por</th><th>Estado</th><th>Fecha de resolución</th>
                      <th>Resolución</th><th>Acciones</th>
                    </tr>
                  } @else if (resource === 'usuarios') {
                    <tr>
                      <th>Nombre</th>
                      <th>Correo</th>
                      <th>Perfil</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  } @else {
                    <tr>
                      <th>ID</th>
                      <th>Detalle</th>
                      <th>Acciones</th>
                    </tr>
                  }
                </thead>
                <tbody>
                  @for (item of visibleItems; track item['id']) {
                    @if (resource === 'turnos') {
                      <tr>
                        <td>{{ turnoUsuarioNombre(item) }}</td>
                        <td>{{ $any(item['fechaInicio']) | date:'dd/MM/yyyy HH:mm' }}</td>
                        <td>{{ item['fechaCierre'] ? ($any(item['fechaCierre']) | date:'dd/MM/yyyy HH:mm') : 'En curso' }}</td>
                        <td>{{ item['estado'] }}</td>
                        <td class="actions">
                          @if (isConserje && item['estado'] === 'ABIERTO') {
                            <button type="button" (click)="cerrarTurno()">Cerrar turno</button>
                          } @else {
                            <span>—</span>
                          }
                        </td>
                      </tr>
                    } @else if (resource === 'visitas') {
                      <tr>
                        <td>{{ item['nombreVisitante'] }}</td>
                        <td>{{ item['documento'] }}</td>
                        <td>{{ item['patente'] || '—' }}</td>
                        <td>{{ visitaDepartamento(item) }}</td>
                        <td>{{ $any(item['fechaIngreso']) | date:'dd/MM/yyyy HH:mm' }}</td>
                        <td>{{ item['fechaSalida'] ? ($any(item['fechaSalida']) | date:'dd/MM/yyyy HH:mm') : 'En curso' }}</td>
                        <td>{{ item['estado'] }}</td>
                        <td class="actions">
                          @if (isConserje && item['estado'] === 'INGRESADA') {
                            <button type="button" (click)="registrarSalidaVisita(item)">Registrar salida</button>
                          } @else {
                            <span>—</span>
                          }
                        </td>
                      </tr>
                    } @else if (resource === 'encomiendas') {
                      <tr>
                        <td>{{ item['destinatario'] }}</td>
                        <td>{{ encomiendaDepartamento(item) }}</td>
                        <td>{{ item['descripcion'] }}</td>
                        <td>{{ item['empresaRepartidor'] || '—' }}</td>
                        <td>{{ $any(item['fechaRecepcion']) | date:'dd/MM/yyyy HH:mm' }}</td>
                        <td>{{ encomiendaConserje(item, 'turnoRecepcion') }}</td>
                        <td>{{ item['fechaEntrega'] ? ($any(item['fechaEntrega']) | date:'dd/MM/yyyy HH:mm') : 'Pendiente' }}</td>
                        <td>{{ item['fechaEntrega'] ? encomiendaConserje(item, 'turnoEntrega') : '—' }}</td>
                        <td>{{ item['entregadoA'] || '—' }}</td>
                        <td>{{ item['estado'] }}</td>
                        <td class="actions">
                          @if (isConserje && item['estado'] === 'PENDIENTE') {
                            <button type="button" (click)="registrarEntregaEncomienda(item)">Registrar entrega</button>
                          } @else { <span>—</span> }
                        </td>
                      </tr>
                    } @else if (resource === 'incidencias') {
                      <tr>
                        <td>{{ $any(item['fechaRegistro']) | date:'dd/MM/yyyy HH:mm' }}</td>
                        <td>{{ item['titulo'] }}</td>
                        <td class="incidencia-descripcion">{{ item['descripcion'] }}</td>
                        <td>{{ item['categoria'] }}</td><td>{{ item['criticidad'] }}</td>
                        <td>{{ incidenciaUsuario(item, 'registradaPor') }}</td>
                        <td>{{ incidenciaEstadoLabel(item['estado']) }}</td>
                        <td>{{ item['fechaResolucion'] ? ($any(item['fechaResolucion']) | date:'dd/MM/yyyy HH:mm') : 'Pendiente' }}</td>
                        <td class="incidencia-resolucion">{{ item['resolucion'] || '—' }}</td>
                        <td class="actions">
                          @if (!isConserje && item['estado'] === 'ABIERTA') {
                            <button type="button" class="secondary-button" (click)="iniciarGestionIncidencia(item)">Iniciar gestión</button>
                          }
                          @if (!isConserje && (item['estado'] === 'ABIERTA' || item['estado'] === 'EN_PROCESO')) {
                            <button type="button" (click)="resolverIncidencia(item)">Resolver</button>
                          }
                          @if (isConserje || item['estado'] === 'RESUELTA') { <span>—</span> }
                        </td>
                      </tr>
                    } @else if (resource === 'usuarios') {
                      <tr>
                        <td>{{ item['nombre'] }}</td>
                        <td>{{ item['email'] }}</td>
                        <td>{{ profileName(item) }}</td>
                        <td>{{ item['activo'] ? 'ACTIVO' : 'INACTIVO' }}</td>
                        <td class="actions">
                          <button type="button" class="secondary-button" (click)="edit(item)">Editar</button>
                          <button type="button" (click)="changeUsuarioEstado(item)">
                            {{ item['activo'] ? 'Desactivar' : 'Reactivar' }}
                          </button>
                        </td>
                      </tr>
                    } @else {
                      <tr>
                        <td>{{ item['id'] }}</td>
                        <td><pre>{{ item | json }}</pre></td>
                        <td class="actions">
                          <button type="button" class="secondary-button" (click)="edit(item)">Editar</button>
                          @if (resource === 'turnos' && item['estado'] === 'ABIERTO') {
                            <button type="button" (click)="patch(item['id'], 'cerrar', { observaciones: 'Cierre desde frontend' })">Cerrar</button>
                          }
                          @if (resource === 'visitas' && item['estado'] === 'INGRESADA') {
                            <button type="button" (click)="patch(item['id'], 'salida')">Salida</button>
                          }
                          @if (resource === 'encomiendas' && item['estado'] === 'PENDIENTE') {
                            <button type="button" (click)="patch(item['id'], 'entregar', { entregadoA: 'Residente' })">Entregar</button>
                          }
                          @if (resource === 'incidencias' && item['estado'] === 'ABIERTA') {
                            <button type="button" (click)="patch(item['id'], 'cerrar')">Cerrar</button>
                          }
                          <button type="button" class="danger-button" (click)="remove(item)">Eliminar</button>
                        </td>
                      </tr>
                    }
                  }
                </tbody>
              </table>
            </div>
          </section>
        </section>
      }
    </main>
  `,
  styles: [`
    .editor-layout.encomiendas-consulta,
    .editor-layout.incidencias-gestion {
      grid-template-columns: minmax(0, 1fr);
    }

    .incidencia-filtros {
      display: grid;
      gap: 1rem;
      grid-template-columns: repeat(3, minmax(180px, 1fr));
      margin-bottom: 1rem;
    }

    .incidencia-descripcion {
      min-width: 20rem;
      overflow-wrap: anywhere;
      white-space: normal;
    }

    .incidencia-resolucion {
      min-width: 18rem;
      overflow-wrap: anywhere;
      white-space: normal;
    }

    [role="combobox"] {
      position: relative;
    }

    [role="combobox"] > input {
      width: 100%;
    }

    [role="listbox"] {
      display: flex;
      flex-direction: column;
      margin-top: 0.5rem;
      max-height: 20rem;
      overflow-y: auto;
      width: 100%;
    }

    [role="listbox"] button {
      flex: 0 0 2.5rem;
      text-align: left;
    }

    [role="listbox"] p {
      margin: 0;
      padding: 0.75rem;
    }
  `]
})
export class ManagementComponent implements OnInit {
  title = '';
  resource = '';
  fields: FieldConfig[] = [];
  items: Record<string, unknown>[] = [];
  reportEntries: Array<{ key: string; label: string; value: number }> = [];
  departmentOptions: SelectOption[] = [];
  profileOptions: SelectOption[] = [];
  filteredDepartmentOptions: SelectOption[] = [];
  departmentSearch = '';
  departmentSuggestionsVisible = false;
  turnoEnCurso: Record<string, unknown> | null = null;
  editingId: number | null = null;
  loading = false;
  refreshing = false;
  error = '';
  success = '';
  encomiendaEstado: 'TODAS' | 'PENDIENTE' | 'ENTREGADA' = 'TODAS';
  incidenciaEstado = 'TODAS';
  incidenciaCategoria = 'TODAS';
  incidenciaCriticidad = 'TODAS';
  private readonly resolucionesPendientes: Record<number, string> = {};

  readonly form: UntypedFormGroup = this.fb.group({});

  get isConserje(): boolean {
    return this.auth.currentUser()?.perfil === 'CONSERJE';
  }

  get currentUserName(): string {
    return this.auth.currentUser()?.nombre ?? '';
  }

  get visibleItems(): Record<string, unknown>[] {
    if (this.resource === 'encomiendas') {
      return this.encomiendaEstado === 'TODAS'
        ? this.items : this.items.filter((item) => item['estado'] === this.encomiendaEstado);
    }
    if (this.resource === 'incidencias') {
      return this.items.filter((item) =>
        (this.incidenciaEstado === 'TODAS' || item['estado'] === this.incidenciaEstado)
        && (this.incidenciaCategoria === 'TODAS' || item['categoria'] === this.incidenciaCategoria)
        && (this.incidenciaCriticidad === 'TODAS' || item['criticidad'] === this.incidenciaCriticidad));
    }
    return this.items;
  }

  onEncomiendaEstadoChange(value: string): void {
    if (value === 'PENDIENTE' || value === 'ENTREGADA' || value === 'TODAS') {
      this.encomiendaEstado = value;
    }
  }

  private readonly fieldMap: Record<string, FieldConfig[]> = {
    perfiles: [
      { key: 'nombre', label: 'Nombre', type: 'text', required: true },
      { key: 'descripcion', label: 'Descripcion', type: 'textarea' },
      { key: 'activo', label: 'Activo', type: 'checkbox' }
    ],
    usuarios: [
      { key: 'nombre', label: 'Nombre completo', type: 'text', required: true, maxLength: 160 },
      { key: 'email', label: 'Correo electrónico', type: 'email', required: true, maxLength: 160 },
      { key: 'password', label: 'Contraseña inicial', type: 'password', required: true, maxLength: 255 },
      { key: 'perfilId', label: 'Perfil', type: 'select', required: true }
    ],
    departamentos: [
      { key: 'torre', label: 'Torre', type: 'text', required: true, maxLength: 40 },
      { key: 'numero', label: 'Numero', type: 'text', required: true, maxLength: 40 },
      { key: 'piso', label: 'Piso', type: 'number', min: -32768, max: 32767 },
      {
        key: 'estado', label: 'Estado', type: 'select', required: true,
        options: [
          { value: 'HABITADO', label: 'HABITADO' },
          { value: 'DESOCUPADO', label: 'DESOCUPADO' }
        ]
      },
      { key: 'observaciones', label: 'Observaciones', type: 'textarea', maxLength: 500 }
    ],
    residentes: [
      { key: 'nombres', label: 'Nombres', type: 'text', required: true, maxLength: 100 },
      { key: 'apellidos', label: 'Apellidos', type: 'text', required: true, maxLength: 100 },
      { key: 'rut', label: 'RUT', type: 'text', maxLength: 20 },
      { key: 'telefono', label: 'Telefono', type: 'text', maxLength: 30 },
      { key: 'email', label: 'Email', type: 'email', maxLength: 160 },
      {
        key: 'tipoResidente', label: 'Tipo residente', type: 'select', required: true,
        options: [
          { value: 'PROPIETARIO', label: 'PROPIETARIO' },
          { value: 'ARRENDATARIO', label: 'ARRENDATARIO' },
          { value: 'FAMILIAR', label: 'FAMILIAR' },
          { value: 'OTRO', label: 'OTRO' }
        ]
      },
      { key: 'departamentoId', label: 'Departamento', type: 'select', required: true },
      { key: 'activo', label: 'Activo', type: 'checkbox' }
    ],
    turnos: [],
    visitas: [
      { key: 'nombreVisitante', label: 'Nombre completo del visitante', type: 'text', required: true, maxLength: 160 },
      { key: 'documento', label: 'Documento o RUT', type: 'text', required: true, maxLength: 40 },
      { key: 'patente', label: 'Patente', type: 'text', maxLength: 20 },
      { key: 'departamentoId', label: 'Departamento', type: 'select', required: true }
    ],
    encomiendas: [
      { key: 'destinatario', label: 'Destinatario', type: 'text', required: true, maxLength: 160 },
      { key: 'departamentoId', label: 'Departamento', type: 'select', required: true },
      { key: 'descripcion', label: 'Descripción', type: 'textarea', required: true, maxLength: 255 },
      { key: 'empresaRepartidor', label: 'Empresa o repartidor', type: 'text', maxLength: 160 }
    ],
    incidencias: [
      { key: 'titulo', label: 'Título', type: 'text', required: true, maxLength: 140 },
      { key: 'descripcion', label: 'Descripción', type: 'textarea', required: true, maxLength: 1000 },
      { key: 'categoria', label: 'Categoría', type: 'select', required: true, options: [
        { value: 'SEGURIDAD', label: 'Seguridad' }, { value: 'INFRAESTRUCTURA', label: 'Infraestructura' },
        { value: 'ACCESO', label: 'Acceso' }, { value: 'RUIDO O CONVIVENCIA', label: 'Ruido o convivencia' },
        { value: 'SERVICIOS', label: 'Servicios' }, { value: 'OTRO', label: 'Otro' }
      ] },
      { key: 'criticidad', label: 'Criticidad', type: 'select', required: true, options: [
        { value: 'BAJA', label: 'Baja' }, { value: 'MEDIA', label: 'Media' },
        { value: 'ALTA', label: 'Alta' }, { value: 'CRÍTICA', label: 'Crítica' }
      ] }
    ]
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: UntypedFormBuilder,
    private readonly api: ApiResourceService,
    private readonly auth: AuthService,
    private readonly changeDetector: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.title = String(data['title'] ?? '');
      this.resource = String(data['resource'] ?? '');
      this.fields = this.fieldMap[this.resource] ?? [];
      this.configureForm();
      this.load();
    });
  }

  refresh(): void {
    this.refreshing = true;
    this.error = '';
    this.success = '';
    this.load(true, false);
  }

  load(showRefreshFeedback = false, reloadRelations = true): void {
    this.error = '';
    if ((this.resource === 'residentes' || this.resource === 'visitas' || this.resource === 'encomiendas') && reloadRelations) {
      this.loadDepartmentOptions();
    }
    if (this.resource === 'usuarios' && reloadRelations) {
      this.loadProfileOptions();
    }
    if (this.resource === 'turnos') {
      this.loadTurnos(showRefreshFeedback);
      return;
    }
    if (this.resource === 'visitas') {
      this.loadVisitas(showRefreshFeedback);
      return;
    }
    if (this.resource === 'encomiendas') {
      this.loadEncomiendas(showRefreshFeedback);
      return;
    }
    if (this.resource === 'incidencias') {
      this.loadIncidencias(showRefreshFeedback);
      return;
    }
    if (this.resource === 'reportes') {
      this.api.path<ReporteResumen>('reportes/resumen').subscribe({
        next: (summary) => {
          this.reportEntries = [
            { key: 'residentesActivos', label: 'Residentes activos', value: summary.residentesActivos },
            { key: 'turnosAbiertos', label: 'Turnos abiertos', value: summary.turnosAbiertos },
            { key: 'visitasDentro', label: 'Visitas dentro', value: summary.visitasDentro },
            { key: 'encomiendasPendientes', label: 'Encomiendas pendientes', value: summary.encomiendasPendientes },
            { key: 'incidenciasAbiertas', label: 'Incidencias abiertas', value: summary.incidenciasAbiertas }
          ];
          this.finishRefresh(showRefreshFeedback, true);
          this.changeDetector.markForCheck();
        },
        error: () => {
          this.error = 'No se pudieron cargar los reportes.';
          this.finishRefresh(showRefreshFeedback, false);
          this.changeDetector.markForCheck();
        }
      });
      return;
    }

    this.api.list<Record<string, unknown>>(this.resource).subscribe({
      next: (items) => {
        this.items = items;
        this.finishRefresh(showRefreshFeedback, true);
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudieron cargar los registros. Verifica la conexión con el backend.';
        this.finishRefresh(showRefreshFeedback, false);
        this.changeDetector.markForCheck();
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    const payload = this.toApiPayload(this.form.getRawValue());
    const wasEditing = this.editingId !== null;
    const request = this.editingId
      ? this.api.update(this.resource, this.editingId, payload)
      : this.api.create(this.resource, payload);

    if (this.resource === 'departamentos' || this.resource === 'residentes' || this.resource === 'usuarios') {
      request.pipe(
        switchMap(() => this.api.list<Record<string, unknown>>(this.resource))
      ).subscribe({
        next: (items) => {
          this.items = items;
          this.loading = false;
          this.resetForm();
          this.success = this.resource === 'usuarios'
            ? (wasEditing ? 'Usuario actualizado correctamente.' : 'Usuario creado correctamente.')
            : (wasEditing ? 'Registro actualizado correctamente.' : 'Registro guardado correctamente.');
          if (this.resource === 'residentes') {
            this.loadDepartmentOptions();
          }
          if (this.resource === 'usuarios') {
            this.loadProfileOptions();
          }
          this.changeDetector.markForCheck();
        },
        error: (error) => {
          this.loading = false;
          this.error = this.errorMessage(error);
          this.changeDetector.markForCheck();
        }
      });
      return;
    }

    request.subscribe({
      next: () => {
        this.loading = false;
        this.resetForm();
        this.load();
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo guardar. Revisa datos relacionados como IDs de perfil, usuario o departamento.';
        this.changeDetector.markForCheck();
      }
    });
  }

  edit(item: Record<string, unknown>): void {
    this.error = '';
    this.success = '';
    this.editingId = Number(item['id']);
    this.form.patchValue(this.toFormValue(item));
    if (this.resource === 'usuarios') {
      this.form.get('password')?.setValue('');
      this.configureUsuarioPassword(false);
    }
    if (this.resource === 'residentes') {
      this.syncDepartmentSearch();
    }
  }

  remove(item: Record<string, unknown>): void {
    if (!window.confirm(this.deleteConfirmation(item))) {
      return;
    }

    this.error = '';
    this.success = '';
    this.api.delete(this.resource, Number(item['id'])).pipe(
      switchMap(() => this.api.list<Record<string, unknown>>(this.resource))
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.success = 'Registro eliminado correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.deleteErrorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  patch(id: unknown, action: string, payload: unknown = {}): void {
    this.api.patch(this.resource, Number(id), action, payload).subscribe({ next: () => this.load() });
  }

  iniciarTurno(): void {
    const user = this.auth.currentUser();
    if (!user) {
      this.error = 'El usuario no existe.';
      this.changeDetector.markForCheck();
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    const payload = {
      usuarioId: user.id
    };
    this.api.create<Record<string, unknown>>('turnos/iniciar', payload).pipe(
      switchMap(() => this.turnoListRequest())
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.turnoEnCurso = items.find((item) => item['estado'] === 'ABIERTO') ?? null;
        this.loading = false;
        this.success = 'Turno iniciado correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  cerrarTurno(): void {
    const user = this.auth.currentUser();
    if (!user || !this.turnoEnCurso) {
      this.error = 'No existe un turno abierto para cerrar.';
      this.changeDetector.markForCheck();
      return;
    }
    if (!window.confirm('¿Seguro que deseas cerrar el turno en curso?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    const payload = {
      usuarioId: user.id
    };
    this.api.patch<Record<string, unknown>>(
      'turnos', Number(this.turnoEnCurso['id']), 'cerrar', payload
    ).pipe(
      switchMap(() => this.turnoListRequest())
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.turnoEnCurso = null;
        this.loading = false;
        this.success = 'Turno cerrado correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  turnoUsuarioNombre(item: Record<string, unknown>): string {
    const usuario = item['usuario'];
    return usuario && typeof usuario === 'object' && 'nombre' in usuario
      ? String((usuario as { nombre?: unknown }).nombre ?? '')
      : '';
  }

  registrarVisita(): void {
    const user = this.auth.currentUser();
    if (!user) {
      this.error = 'El usuario no existe.';
      this.changeDetector.markForCheck();
      return;
    }
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    const raw = this.form.getRawValue();
    const payload = {
      usuarioId: user.id,
      departamentoId: Number(raw['departamentoId']),
      nombreVisitante: raw['nombreVisitante'],
      documento: raw['documento'],
      patente: raw['patente'] || null
    };
    this.api.create<Record<string, unknown>>('visitas/ingreso', payload).pipe(
      switchMap(() => this.visitaListRequest())
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.resetForm();
        this.success = 'Visita registrada correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  registrarEncomienda(): void {
    const user = this.auth.currentUser();
    if (!user) {
      this.error = 'El usuario no existe.';
      return;
    }
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.create<Record<string, unknown>>('encomiendas/recepcion', {
      usuarioId: user.id,
      departamentoId: Number(raw['departamentoId']),
      destinatario: raw['destinatario'],
      descripcion: raw['descripcion'],
      empresaRepartidor: raw['empresaRepartidor'] || null
    }).pipe(switchMap(() => this.encomiendaListRequest())).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.resetForm();
        this.success = 'Encomienda registrada correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  registrarEntregaEncomienda(item: Record<string, unknown>): void {
    const user = this.auth.currentUser();
    if (!user) {
      this.error = 'El usuario no existe.';
      return;
    }
    const destinatario = String(item['destinatario'] ?? '');
    const departamento = this.encomiendaDepartamento(item);
    if (!window.confirm(`¿Registrar la entrega para ${destinatario}, departamento ${departamento}?`)) return;
    const entregadoA = window.prompt('Entregado a');
    if (entregadoA === null) return;
    if (!entregadoA.trim()) {
      this.error = 'Debes indicar quién retira la encomienda.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.patch<Record<string, unknown>>('encomiendas', Number(item['id']), 'entregar', {
      usuarioId: user.id,
      entregadoA
    }).pipe(switchMap(() => this.encomiendaListRequest())).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.success = 'Entrega registrada correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  encomiendaDepartamento(item: Record<string, unknown>): string {
    return this.visitaDepartamento(item);
  }

  encomiendaConserje(item: Record<string, unknown>, turnoKey: string): string {
    const turno = item[turnoKey];
    if (!turno || typeof turno !== 'object' || !('usuario' in turno)) return '—';
    const usuario = (turno as { usuario?: unknown }).usuario;
    return usuario && typeof usuario === 'object' && 'nombre' in usuario
      ? String((usuario as { nombre?: unknown }).nombre ?? '—') : '—';
  }

  registrarIncidencia(): void {
    const user = this.auth.currentUser();
    if (!user) { this.error = 'El usuario no existe.'; return; }
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.create<Record<string, unknown>>('incidencias/registro', {
      usuarioId: user.id,
      titulo: raw['titulo'],
      descripcion: raw['descripcion'],
      categoria: raw['categoria'],
      criticidad: raw['criticidad']
    }).pipe(switchMap(() => this.incidenciaListRequest())).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.resetForm();
        this.success = 'Incidencia registrada correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.incidenciaErrorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  iniciarGestionIncidencia(item: Record<string, unknown>): void {
    const user = this.auth.currentUser();
    if (!user) { this.error = 'El usuario no existe.'; return; }
    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.patch<Record<string, unknown>>('incidencias', Number(item['id']), 'iniciar-gestion', {
      usuarioId: user.id
    }).pipe(switchMap(() => this.incidenciaListRequest())).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.success = 'Incidencia puesta en proceso correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.incidenciaErrorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  resolverIncidencia(item: Record<string, unknown>): void {
    const user = this.auth.currentUser();
    if (!user) { this.error = 'El usuario no existe.'; return; }
    const id = Number(item['id']);
    const resolucion = window.prompt('Resolución o medida tomada', this.resolucionesPendientes[id] ?? '');
    if (resolucion === null) return;
    this.resolucionesPendientes[id] = resolucion;
    if (!resolucion.trim()) {
      this.error = 'Debes indicar la resolución o medida tomada.';
      return;
    }
    if (!window.confirm(`¿Confirmas que deseas marcar como resuelta la incidencia "${String(item['titulo'] ?? '')}"?`)) return;
    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.patch<Record<string, unknown>>('incidencias', id, 'resolver', {
      usuarioId: user.id,
      resolucion
    }).pipe(switchMap(() => this.incidenciaListRequest())).subscribe({
      next: (items) => {
        delete this.resolucionesPendientes[id];
        this.items = items;
        this.loading = false;
        this.success = 'Incidencia resuelta correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.incidenciaErrorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  incidenciaUsuario(item: Record<string, unknown>, key: string): string {
    const usuario = item[key];
    return usuario && typeof usuario === 'object' && 'nombre' in usuario
      ? String((usuario as { nombre?: unknown }).nombre ?? '—') : '—';
  }

  incidenciaEstadoLabel(value: unknown): string {
    return value === 'EN_PROCESO' ? 'EN PROCESO' : String(value ?? '');
  }

  registrarSalidaVisita(item: Record<string, unknown>): void {
    const user = this.auth.currentUser();
    if (!user) {
      this.error = 'El usuario no existe.';
      this.changeDetector.markForCheck();
      return;
    }
    const visitor = String(item['nombreVisitante'] ?? '');
    if (!window.confirm(`¿Seguro que deseas registrar la salida de ${visitor}?`)) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    this.api.patch<Record<string, unknown>>(
      'visitas', Number(item['id']), 'salida', { usuarioId: user.id }
    ).pipe(
      switchMap(() => this.visitaListRequest())
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
        this.success = 'Salida registrada correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  visitaDepartamento(item: Record<string, unknown>): string {
    const departamento = item['departamento'];
    if (!departamento || typeof departamento !== 'object') {
      return '';
    }
    const value = departamento as { torre?: unknown; numero?: unknown };
    return `${String(value.torre ?? '')} - ${String(value.numero ?? '')}`;
  }

  profileName(item: Record<string, unknown>): string {
    const perfil = item['perfil'];
    return perfil && typeof perfil === 'object' && 'nombre' in perfil
      ? String((perfil as { nombre?: unknown }).nombre ?? '')
      : '';
  }

  changeUsuarioEstado(item: Record<string, unknown>): void {
    const active = Boolean(item['activo']);
    const action = active ? 'desactivar' : 'reactivar';
    const name = String(item['nombre'] ?? '');
    if (!window.confirm(`¿Seguro que deseas ${action} a ${name}?`)) {
      return;
    }

    this.error = '';
    this.success = '';
    this.api.patch('usuarios', Number(item['id']), action).pipe(
      switchMap(() => this.api.list<Record<string, unknown>>('usuarios'))
    ).subscribe({
      next: (items) => {
        this.items = items;
        this.success = active
          ? 'Usuario desactivado correctamente.'
          : 'Usuario reactivado correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      }
    });
  }

  optionsFor(field: FieldConfig): SelectOption[] {
    return field.key === 'perfilId' ? this.profileOptions : field.options ?? [];
  }

  onDepartmentSearch(search: string): void {
    this.departmentSearch = search;
    this.form.get('departamentoId')?.setValue('');
    this.form.get('departamentoId')?.markAsTouched();
    this.updateDepartmentSuggestions();
    this.departmentSuggestionsVisible = search.trim().length > 0;
  }

  showDepartmentSuggestions(): void {
    this.updateDepartmentSuggestions();
    this.departmentSuggestionsVisible = this.departmentSearch.trim().length > 0;
  }

  selectDepartment(option: SelectOption): void {
    this.departmentSearch = option.label;
    this.form.get('departamentoId')?.setValue(Number(option.value));
    this.form.get('departamentoId')?.markAsTouched();
    this.departmentSuggestionsVisible = false;
  }

  private updateDepartmentSuggestions(): void {
    const search = this.departmentSearch;
    const normalizedSearch = search.trim().toLocaleLowerCase();
    this.filteredDepartmentOptions = normalizedSearch
      ? this.departmentOptions.filter((option) => option.label.toLocaleLowerCase().includes(normalizedSearch))
          .slice(0, 8)
      : [];
  }

  resetForm(): void {
    this.editingId = null;
    this.departmentSearch = '';
    this.departmentSuggestionsVisible = false;
    this.updateDepartmentSuggestions();
    this.form.reset(this.defaultValues());
    if (this.resource === 'usuarios') {
      this.configureUsuarioPassword(true);
    }
  }

  private configureForm(): void {
    for (const control of Object.keys(this.form.controls)) {
      this.form.removeControl(control);
    }
    for (const field of this.fields) {
      const validators: ValidatorFn[] = [];
      if (field.required) {
        validators.push(Validators.required);
      }
      if (field.maxLength !== undefined) {
        validators.push(Validators.maxLength(field.maxLength));
      }
      if (field.type === 'email') {
        validators.push(Validators.email);
      }
      if (field.min !== undefined) {
        validators.push(Validators.min(field.min));
      }
      if (field.max !== undefined) {
        validators.push(Validators.max(field.max));
      }
      if (field.key === 'departamentoId') {
        validators.push((control) => {
          const id = Number(control.value);
          return this.departmentOptions.some((option) => Number(option.value) === id)
            ? null
            : { departamentoInvalido: true };
        });
      }
      if (field.key === 'perfilId') {
        validators.push((control) => {
          const id = Number(control.value);
          return this.profileOptions.some((option) => Number(option.value) === id)
            ? null
            : { perfilInvalido: true };
        });
      }
      this.form.addControl(field.key, this.fb.control(this.defaultValue(field), validators));
    }
  }

  private loadDepartmentOptions(): void {
    this.api.list<Record<string, unknown>>('departamentos').subscribe({
      next: (departments) => {
        this.departmentOptions = departments
          .map((department) => ({
            value: Number(department['id']),
            label: `${String(department['torre'] ?? '')} - ${String(department['numero'] ?? '')}`
          }))
          .sort((a, b) => a.label.localeCompare(b.label));
        this.updateDepartmentSuggestions();
        this.syncDepartmentSearch();
        this.form.get('departamentoId')?.updateValueAndValidity();
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudieron cargar los departamentos.';
        this.changeDetector.markForCheck();
      }
    });
  }

  private loadTurnos(showRefreshFeedback: boolean): void {
    this.turnoListRequest().subscribe({
      next: (items) => {
        this.items = items;
        this.turnoEnCurso = this.isConserje
          ? items.find((item) => item['estado'] === 'ABIERTO') ?? null
          : null;
        this.finishRefresh(showRefreshFeedback, true);
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.errorMessage(error);
        this.finishRefresh(showRefreshFeedback, false);
        this.changeDetector.markForCheck();
      }
    });
  }

  private loadVisitas(showRefreshFeedback: boolean): void {
    this.visitaListRequest().subscribe({
      next: (items) => {
        this.items = items;
        this.finishRefresh(showRefreshFeedback, true);
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.errorMessage(error);
        this.finishRefresh(showRefreshFeedback, false);
        this.changeDetector.markForCheck();
      }
    });
  }

  private loadEncomiendas(showRefreshFeedback: boolean): void {
    this.encomiendaListRequest().subscribe({
      next: (items) => {
        this.items = items;
        this.finishRefresh(showRefreshFeedback, true);
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.errorMessage(error);
        this.finishRefresh(showRefreshFeedback, false);
        this.changeDetector.markForCheck();
      }
    });
  }

  private loadIncidencias(showRefreshFeedback: boolean): void {
    this.incidenciaListRequest().subscribe({
      next: (items) => {
        this.items = items;
        this.finishRefresh(showRefreshFeedback, true);
        this.changeDetector.markForCheck();
      },
      error: (error) => {
        this.error = this.incidenciaErrorMessage(error);
        this.finishRefresh(showRefreshFeedback, false);
        this.changeDetector.markForCheck();
      }
    });
  }

  private incidenciaListRequest(): Observable<Record<string, unknown>[]> {
    return this.isConserje
      ? this.api.path<Record<string, unknown>[]>('incidencias/mes-actual')
      : this.api.list<Record<string, unknown>>('incidencias');
  }

  private encomiendaListRequest(): Observable<Record<string, unknown>[]> {
    return this.isConserje
      ? this.api.path<Record<string, unknown>[]>('encomiendas/mes-actual')
      : this.api.list<Record<string, unknown>>('encomiendas');
  }

  private visitaListRequest(): Observable<Record<string, unknown>[]> {
    return this.isConserje
      ? this.api.path<Record<string, unknown>[]>('visitas/activas')
      : this.api.list<Record<string, unknown>>('visitas');
  }

  private turnoListRequest(): Observable<Record<string, unknown>[]> {
    const user = this.auth.currentUser();
    return this.isConserje && user
      ? this.api.path<Record<string, unknown>[]>(`turnos/usuario/${user.id}`)
      : this.api.list<Record<string, unknown>>('turnos');
  }

  private loadProfileOptions(): void {
    this.api.list<Record<string, unknown>>('perfiles').subscribe({
      next: (profiles) => {
        this.profileOptions = profiles
          .filter((profile) => profile['activo'] === true)
          .map((profile) => ({
            value: Number(profile['id']),
            label: String(profile['nombre'] ?? '')
          }))
          .sort((a, b) => a.label.localeCompare(b.label));
        this.form.get('perfilId')?.updateValueAndValidity();
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudieron cargar los perfiles.';
        this.changeDetector.markForCheck();
      }
    });
  }

  private configureUsuarioPassword(required: boolean): void {
    const control = this.form.get('password');
    if (!control) {
      return;
    }
    const validators: ValidatorFn[] = [Validators.maxLength(255)];
    if (required) {
      validators.unshift(Validators.required);
    }
    control.setValidators(validators);
    control.updateValueAndValidity();
  }

  private defaultValues(): Record<string, unknown> {
    return this.fields.reduce<Record<string, unknown>>((acc, field) => {
      acc[field.key] = this.defaultValue(field);
      return acc;
    }, {});
  }

  private defaultValue(field: FieldConfig): unknown {
    return field.initialValue ?? (field.type === 'checkbox' ? true : '');
  }

  private toApiPayload(raw: Partial<Record<string, unknown>>): Record<string, unknown> {
    const payload: Record<string, unknown> = { ...raw };
    if (this.resource === 'usuarios' && this.editingId !== null
        && (payload['password'] === null || payload['password'] === undefined || payload['password'] === '')) {
      delete payload['password'];
    }
    this.mapRelation(payload, 'perfilId', 'perfil');
    this.mapRelation(payload, 'departamentoId', 'departamento');
    this.mapRelation(payload, 'usuarioId', 'usuario');
    this.mapRelation(payload, 'residenteAutorizadorId', 'residenteAutorizador');
    this.mapRelation(payload, 'registradaPorId', 'registradaPor');
    return payload;
  }

  private mapRelation(payload: Record<string, unknown>, source: string, target: string): void {
    const value = payload[source];
    delete payload[source];
    if (value !== undefined && value !== null && value !== '') {
      payload[target] = { id: Number(value) };
    }
  }

  private toFormValue(item: Record<string, unknown>): Record<string, unknown> {
    const value: Record<string, unknown> = { ...item };
    value['perfilId'] = this.extractId(item['perfil']);
    value['departamentoId'] = this.extractId(item['departamento']);
    value['usuarioId'] = this.extractId(item['usuario']);
    value['residenteAutorizadorId'] = this.extractId(item['residenteAutorizador']);
    value['registradaPorId'] = this.extractId(item['registradaPor']);
    return value;
  }

  private extractId(value: unknown): number | '' {
    if (value && typeof value === 'object' && 'id' in value) {
      return Number((value as { id: number }).id);
    }
    return '';
  }

  private syncDepartmentSearch(): void {
    const selectedId = Number(this.form.get('departamentoId')?.value);
    const selected = this.departmentOptions.find((option) => Number(option.value) === selectedId);
    this.departmentSearch = selected?.label ?? '';
    this.departmentSuggestionsVisible = false;
    this.updateDepartmentSuggestions();
  }

  private errorMessage(error: unknown): string {
    if (error && typeof error === 'object' && 'error' in error) {
      const body = (error as { error?: unknown }).error;
      if (body && typeof body === 'object' && 'message' in body) {
        const message = (body as { message?: unknown }).message;
        if (typeof message === 'string' && message.trim()) {
          return message;
        }
      }
    }
    return 'No se pudo guardar el registro. Revisa los datos ingresados.';
  }

  private incidenciaErrorMessage(error: unknown): string {
    const status = error && typeof error === 'object' && 'status' in error
      ? Number((error as { status?: unknown }).status) : 0;
    const message = this.errorMessage(error);
    if (status >= 500 || /(sql|constraint|column|jdbc|hibernate|value not permitted)/i.test(message)) {
      return 'No se pudo completar la operación. Intenta nuevamente.';
    }
    return message;
  }

  private finishRefresh(showFeedback: boolean, successful: boolean): void {
    if (!showFeedback) {
      return;
    }
    this.refreshing = false;
    if (successful) {
      this.success = 'Registros actualizados correctamente.';
    }
  }

  private deleteConfirmation(item: Record<string, unknown>): string {
    if (this.resource === 'residentes') {
      const nombre = `${String(item['nombres'] ?? '')} ${String(item['apellidos'] ?? '')}`.trim();
      return `¿Seguro que deseas eliminar a ${nombre}?\nEsta acción no se puede deshacer.`;
    }
    if (this.resource === 'departamentos') {
      const departamento = `${String(item['torre'] ?? '')} - ${String(item['numero'] ?? '')}`;
      return `¿Seguro que deseas eliminar el departamento ${departamento}?\nEsta acción no se puede deshacer.`;
    }
    return '¿Seguro que deseas eliminar este registro?\nEsta acción no se puede deshacer.';
  }

  private deleteErrorMessage(error: unknown): string {
    const status = error && typeof error === 'object' && 'status' in error
      ? Number((error as { status?: unknown }).status)
      : 0;
    if (this.resource === 'departamentos' && (status === 409 || status >= 500)) {
      return 'No se puede eliminar el departamento porque tiene residentes relacionados.';
    }
    return this.errorMessage(error).replace('guardar', 'eliminar');
  }
}
