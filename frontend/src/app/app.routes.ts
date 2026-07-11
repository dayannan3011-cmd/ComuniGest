import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ManagementComponent } from './features/management/management.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: DashboardComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'reportes', pathMatch: 'full' },
      { path: 'reportes', component: ManagementComponent, data: { resource: 'reportes', title: 'Reportes' } },
      { path: 'perfiles', component: ManagementComponent, data: { resource: 'perfiles', title: 'Perfiles' } },
      { path: 'usuarios', component: ManagementComponent, data: { resource: 'usuarios', title: 'Usuarios' } },
      { path: 'departamentos', component: ManagementComponent, data: { resource: 'departamentos', title: 'Departamentos' } },
      { path: 'residentes', component: ManagementComponent, data: { resource: 'residentes', title: 'Residentes' } },
      { path: 'turnos', component: ManagementComponent, data: { resource: 'turnos', title: 'Turnos' } },
      { path: 'visitas', component: ManagementComponent, data: { resource: 'visitas', title: 'Visitas' } },
      { path: 'encomiendas', component: ManagementComponent, data: { resource: 'encomiendas', title: 'Encomiendas' } },
      { path: 'incidencias', component: ManagementComponent, data: { resource: 'incidencias', title: 'Incidencias' } }
    ]
  },
  { path: '**', redirectTo: '' }
];
