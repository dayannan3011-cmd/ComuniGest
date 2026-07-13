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
        <section class="editor-layout">
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
            <h2>Registros</h2>
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
                  @for (item of items; track item['id']) {
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

  readonly form: UntypedFormGroup = this.fb.group({});

  get isConserje(): boolean {
    return this.auth.currentUser()?.perfil === 'CONSERJE';
  }

  get currentUserName(): string {
    return this.auth.currentUser()?.nombre ?? '';
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
      { key: 'descripcion', label: 'Descripcion', type: 'textarea', required: true },
      { key: 'codigoRecepcion', label: 'Codigo recepcion', type: 'text' },
      { key: 'recibidoPor', label: 'Recibido por', type: 'text' },
      { key: 'departamentoId', label: 'ID departamento', type: 'number', required: true }
    ],
    incidencias: [
      { key: 'titulo', label: 'Titulo', type: 'text', required: true },
      { key: 'descripcion', label: 'Descripcion', type: 'textarea', required: true },
      { key: 'categoria', label: 'Categoria', type: 'text' },
      { key: 'criticidad', label: 'Criticidad', type: 'text' },
      { key: 'registradaPorId', label: 'ID usuario registra', type: 'number' }
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
    if ((this.resource === 'residentes' || this.resource === 'visitas') && reloadRelations) {
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
