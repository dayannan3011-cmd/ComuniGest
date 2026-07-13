import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <main class="login-page">
      <section class="login-panel">
        <div>
          <p class="eyebrow">ComuniGest</p>
          <h1>Acceso de conserjeria</h1>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="form-grid">
          <label>
            Email
            <input type="email" formControlName="email" autocomplete="username">
          </label>

          <label>
            Clave
            <input type="password" formControlName="password" autocomplete="current-password">
          </label>

          @if (error) {
            <p class="form-error">{{ error }}</p>
          }

          <button type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>

        <p class="hint">Usuario inicial: admin&#64;comunigest.local / admin123</p>
      </section>
    </main>
  `
})
export class LoginComponent {
  loading = false;
  error = '';

  readonly form = this.fb.group({
    email: ['admin@comunigest.local', [Validators.required, Validators.email]],
    password: ['admin123', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly changeDetector: ChangeDetectorRef
  ) {}

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.changeDetector.markForCheck();
    const { email, password } = this.form.getRawValue();
    this.auth.login(email ?? '', password ?? '').subscribe({
      next: (response) => {
        this.loading = false;
        this.changeDetector.markForCheck();
        const destination = response.perfil === 'ADMINISTRADOR' ? '/reportes' : '/turnos';
        this.router.navigateByUrl(destination);
      },
      error: (response) => {
        this.loading = false;
        const backendMessage = response?.error?.message;
        if (response?.status === 401) {
          this.error = 'Correo o clave incorrectos.';
        } else if (backendMessage === 'El usuario se encuentra inactivo.'
            || backendMessage === 'La cuenta se encuentra inactiva.') {
          this.error = 'El usuario se encuentra inactivo.';
        } else if (response?.status === 0) {
          this.error = 'No se pudo conectar con el servidor.';
        } else {
          this.error = typeof backendMessage === 'string' && backendMessage.trim()
            ? backendMessage
            : 'No se pudo conectar con el servidor.';
        }
        this.changeDetector.markForCheck();
      }
    });
  }
}
