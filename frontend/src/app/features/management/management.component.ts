import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { JsonPipe } from '@angular/common';
import { ApiResourceService } from '../../core/api-resource.service';
import { ReporteResumen } from '../../core/models';

type FieldType = 'text' | 'number' | 'email' | 'password' | 'textarea' | 'checkbox';

interface FieldConfig {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
}

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [ReactiveFormsModule, JsonPipe],
  template: `
    <main class="content">
      <div class="content-header">
        <div>
          <p class="eyebrow">Modulo</p>
          <h1>{{ title }}</h1>
        </div>
        <button type="button" class="secondary-button" (click)="load()">Actualizar</button>
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
          <form [formGroup]="form" (ngSubmit)="save()" class="panel form-grid">
            <h2>{{ editingId ? 'Editar registro' : 'Nuevo registro' }}</h2>

            @for (field of fields; track field.key) {
              <label>
                {{ field.label }}
                @if (field.type === 'textarea') {
                  <textarea [formControlName]="field.key" rows="3"></textarea>
                } @else if (field.type === 'checkbox') {
                  <input type="checkbox" [formControlName]="field.key">
                } @else {
                  <input [type]="field.type" [formControlName]="field.key">
                }
              </label>
            }

            @if (error) {
              <p class="form-error">{{ error }}</p>
            }

            <div class="button-row">
              <button type="submit" [disabled]="form.invalid || loading">
                {{ loading ? 'Guardando...' : 'Guardar' }}
              </button>
              <button type="button" class="secondary-button" (click)="resetForm()">Limpiar</button>
            </div>
          </form>

          <section class="panel table-panel">
            <h2>Registros</h2>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Detalle</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  @for (item of items; track item['id']) {
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
                        <button type="button" class="danger-button" (click)="remove(item['id'])">Eliminar</button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </section>
        </section>
      }
    </main>
  `
})
export class ManagementComponent implements OnInit {
  title = '';
  resource = '';
  fields: FieldConfig[] = [];
  items: Record<string, unknown>[] = [];
  reportEntries: Array<{ key: string; label: string; value: number }> = [];
  editingId: number | null = null;
  loading = false;
  error = '';

  readonly form: UntypedFormGroup = this.fb.group({});

  private readonly fieldMap: Record<string, FieldConfig[]> = {
    perfiles: [
      { key: 'nombre', label: 'Nombre', type: 'text', required: true },
      { key: 'descripcion', label: 'Descripcion', type: 'textarea' },
      { key: 'activo', label: 'Activo', type: 'checkbox' }
    ],
    usuarios: [
      { key: 'nombre', label: 'Nombre', type: 'text', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'password', label: 'Clave', type: 'password', required: true },
      { key: 'perfilId', label: 'ID perfil', type: 'number', required: true },
      { key: 'activo', label: 'Activo', type: 'checkbox' }
    ],
    departamentos: [
      { key: 'torre', label: 'Torre', type: 'text', required: true },
      { key: 'numero', label: 'Numero', type: 'text', required: true },
      { key: 'piso', label: 'Piso', type: 'number' },
      { key: 'estado', label: 'Estado', type: 'text' },
      { key: 'observaciones', label: 'Observaciones', type: 'textarea' }
    ],
    residentes: [
      { key: 'nombres', label: 'Nombres', type: 'text', required: true },
      { key: 'rut', label: 'RUT', type: 'text' },
      { key: 'telefono', label: 'Telefono', type: 'text' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'tipoResidente', label: 'Tipo residente', type: 'text' },
      { key: 'departamentoId', label: 'ID departamento', type: 'number', required: true },
      { key: 'activo', label: 'Activo', type: 'checkbox' }
    ],
    turnos: [
      { key: 'usuarioId', label: 'ID usuario', type: 'number', required: true },
      { key: 'observaciones', label: 'Observaciones', type: 'textarea' }
    ],
    visitas: [
      { key: 'nombreVisitante', label: 'Nombre visitante', type: 'text', required: true },
      { key: 'documento', label: 'Documento', type: 'text' },
      { key: 'patente', label: 'Patente', type: 'text' },
      { key: 'motivo', label: 'Motivo', type: 'textarea' },
      { key: 'departamentoId', label: 'ID departamento', type: 'number', required: true },
      { key: 'residenteAutorizadorId', label: 'ID residente autorizador', type: 'number' }
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
    private readonly api: ApiResourceService
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

  load(): void {
    this.error = '';
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
        },
        error: () => this.error = 'No se pudieron cargar los reportes.'
      });
      return;
    }

    this.api.list<Record<string, unknown>>(this.resource).subscribe({
      next: (items) => this.items = items,
      error: () => this.error = 'No se pudieron cargar los registros.'
    });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';
    const payload = this.toApiPayload(this.form.getRawValue());
    const request = this.editingId
      ? this.api.update(this.resource, this.editingId, payload)
      : this.api.create(this.resource, payload);

    request.subscribe({
      next: () => {
        this.loading = false;
        this.resetForm();
        this.load();
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo guardar. Revisa datos relacionados como IDs de perfil, usuario o departamento.';
      }
    });
  }

  edit(item: Record<string, unknown>): void {
    this.editingId = Number(item['id']);
    this.form.patchValue(this.toFormValue(item));
  }

  remove(id: unknown): void {
    this.api.delete(this.resource, Number(id)).subscribe({ next: () => this.load() });
  }

  patch(id: unknown, action: string, payload: unknown = {}): void {
    this.api.patch(this.resource, Number(id), action, payload).subscribe({ next: () => this.load() });
  }

  resetForm(): void {
    this.editingId = null;
    this.form.reset(this.defaultValues());
  }

  private configureForm(): void {
    for (const control of Object.keys(this.form.controls)) {
      this.form.removeControl(control);
    }
    for (const field of this.fields) {
      this.form.addControl(field.key, this.fb.control(this.defaultValue(field), field.required ? Validators.required : []));
    }
  }

  private defaultValues(): Record<string, unknown> {
    return this.fields.reduce<Record<string, unknown>>((acc, field) => {
      acc[field.key] = this.defaultValue(field);
      return acc;
    }, {});
  }

  private defaultValue(field: FieldConfig): unknown {
    return field.type === 'checkbox' ? true : '';
  }

  private toApiPayload(raw: Partial<Record<string, unknown>>): Record<string, unknown> {
    const payload: Record<string, unknown> = { ...raw };
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
}
