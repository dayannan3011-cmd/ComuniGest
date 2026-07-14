import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-recuperar-clave',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="login-page">
      <section class="login-panel">
        <div>
          <p class="eyebrow">ComuniGest</p>
          <h1>Recuperar contraseña</h1>
          <p class="hint">Ingresa tu correo para recibir las instrucciones.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="form-grid">
          <label>
            Email
            <input type="email" formControlName="email" autocomplete="email">
          </label>

          @if (message) {
            <p class="hint">{{ message }}</p>
          }
          @if (error) {
            <p class="form-error">{{ error }}</p>
          }

          <button type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Enviando...' : 'Enviar instrucciones' }}
          </button>
          <a routerLink="/login">Volver al inicio de sesión</a>
        </form>
      </section>
    </main>
  `
})
export class RecuperarClaveComponent {
  loading = false;
  message = '';
  error = '';

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly changeDetector: ChangeDetectorRef
  ) {}

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.message = '';
    this.error = '';
    this.auth.recuperarClave(this.form.getRawValue().email ?? '').subscribe({
      next: (response) => {
        this.loading = false;
        this.message = response.message;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = 'No fue posible procesar la solicitud.';
        this.changeDetector.markForCheck();
      }
    });
  }
}
