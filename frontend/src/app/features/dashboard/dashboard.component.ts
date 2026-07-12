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
          @for (item of visibleNavItems; track item.path) {
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
        @if (auth.accessDeniedMessage()) {
          <div class="form-error" role="alert">
            {{ auth.accessDeniedMessage() }}
            <button type="button" class="secondary-button" (click)="auth.clearAccessDenied()">Cerrar</button>
          </div>
        }
        <router-outlet />
      </section>
    </div>
  `
})
export class DashboardComponent {
  readonly navItems = [
    { path: '/reportes', label: 'Reportes', adminOnly: true },
    { path: '/perfiles', label: 'Perfiles', adminOnly: true },
    { path: '/usuarios', label: 'Usuarios', adminOnly: true },
    { path: '/departamentos', label: 'Departamentos', adminOnly: true },
    { path: '/residentes', label: 'Residentes', adminOnly: true },
    { path: '/turnos', label: 'Turnos', adminOnly: false },
    { path: '/visitas', label: 'Visitas', adminOnly: false },
    { path: '/encomiendas', label: 'Encomiendas', adminOnly: false },
    { path: '/incidencias', label: 'Incidencias', adminOnly: false }
  ];

  get visibleNavItems(): typeof this.navItems {
    return this.auth.currentUser()?.perfil === 'ADMINISTRADOR'
      ? this.navItems
      : this.navItems.filter((item) => !item.adminOnly);
  }

  constructor(public readonly auth: AuthService) {}
}
