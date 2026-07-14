import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-restablecer-clave',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="login-page">
      <section class="login-panel">
        <div>
          <p class="eyebrow">ComuniGest</p>
          <h1>Restablecer contraseña</h1>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="form-grid">
          <label>
            Nueva contraseña
            <input type="password" formControlName="nuevaClave" autocomplete="new-password">
          </label>
          <label>
            Confirmar contraseña
            <input type="password" formControlName="confirmacionClave" autocomplete="new-password">
          </label>

          @if (message) {
            <p class="hint">{{ message }}</p>
          }
          @if (error) {
            <p class="form-error">{{ error }}</p>
          }

          <button type="submit" [disabled]="form.invalid || loading || !token">
            {{ loading ? 'Guardando...' : 'Restablecer contraseña' }}
          </button>
          <a routerLink="/login">Volver al inicio de sesión</a>
        </form>
      </section>
    </main>
  `
})
export class RestablecerClaveComponent {
  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  loading = false;
  message = '';
  error = this.token ? '' : 'El enlace de recuperación no contiene un token.';

  readonly form = this.fb.group({
    nuevaClave: ['', [Validators.required, Validators.maxLength(255)]],
    confirmacionClave: ['', [Validators.required, Validators.maxLength(255)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute,
    private readonly changeDetector: ChangeDetectorRef
  ) {}

  submit(): void {
    if (this.form.invalid || !this.token) return;
    const { nuevaClave, confirmacionClave } = this.form.getRawValue();
    if (nuevaClave !== confirmacionClave) {
      this.error = 'Las claves no coinciden.';
      return;
    }

    this.loading = true;
    this.message = '';
    this.error = '';
    this.auth.restablecerClave(this.token, nuevaClave ?? '', confirmacionClave ?? '').subscribe({
      next: (response) => {
        this.loading = false;
        this.message = response.message;
        this.form.disable();
        this.changeDetector.markForCheck();
      },
      error: (response) => {
        this.loading = false;
        this.error = response?.error?.message ?? 'No fue posible restablecer la contraseña.';
        this.changeDetector.markForCheck();
      }
    });
  }
}
