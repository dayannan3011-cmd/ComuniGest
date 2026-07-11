import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark">CG</span>
          <div>
            <strong>ComuniGest</strong>
            <small>{{ auth.currentUser()?.perfil }}</small>
          </div>
        </div>

        <nav>
          @for (item of navItems; track item.path) {
            <a [routerLink]="item.path" routerLinkActive="active">{{ item.label }}</a>
          }
        </nav>

        <button class="ghost-button" type="button" (click)="auth.logout()">Cerrar sesion</button>
      </aside>

      <section class="workspace">
        <header class="topbar">
          <div>
            <span>Sesion local</span>
            <strong>{{ auth.currentUser()?.nombre }}</strong>
          </div>
        </header>
        <router-outlet />
      </section>
    </div>
  `
})
export class DashboardComponent {
  readonly navItems = [
    { path: '/reportes', label: 'Reportes' },
    { path: '/perfiles', label: 'Perfiles' },
    { path: '/usuarios', label: 'Usuarios' },
    { path: '/departamentos', label: 'Departamentos' },
    { path: '/residentes', label: 'Residentes' },
    { path: '/turnos', label: 'Turnos' },
    { path: '/visitas', label: 'Visitas' },
    { path: '/encomiendas', label: 'Encomiendas' },
    { path: '/incidencias', label: 'Incidencias' }
  ];

  constructor(public readonly auth: AuthService) {}
}
