import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/role.guard';
import { LoginComponent } from './features/auth/login.component';
import { RecuperarClaveComponent } from './features/auth/recuperar-clave.component';
import { RestablecerClaveComponent } from './features/auth/restablecer-clave.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ManagementComponent } from './features/management/management.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'recuperar-clave', component: RecuperarClaveComponent },
  { path: 'restablecer-clave', component: RestablecerClaveComponent },
  {
    path: '',
    component: DashboardComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'reportes', pathMatch: 'full' },
      { path: 'reportes', component: ManagementComponent, canActivate: [adminGuard], data: { resource: 'reportes', title: 'Reportes' } },
      { path: 'perfiles', component: ManagementComponent, canActivate: [adminGuard], data: { resource: 'perfiles', title: 'Perfiles' } },
      { path: 'usuarios', component: ManagementComponent, canActivate: [adminGuard], data: { resource: 'usuarios', title: 'Usuarios' } },
      { path: 'departamentos', component: ManagementComponent, canActivate: [adminGuard], data: { resource: 'departamentos', title: 'Departamentos' } },
      { path: 'residentes', component: ManagementComponent, canActivate: [adminGuard], data: { resource: 'residentes', title: 'Residentes' } },
      { path: 'turnos', component: ManagementComponent, data: { resource: 'turnos', title: 'Turnos' } },
      { path: 'visitas', component: ManagementComponent, data: { resource: 'visitas', title: 'Visitas' } },
      { path: 'encomiendas', component: ManagementComponent, data: { resource: 'encomiendas', title: 'Encomiendas' } },
      { path: 'incidencias', component: ManagementComponent, data: { resource: 'incidencias', title: 'Incidencias' } }
    ]
  },
  { path: '**', redirectTo: '' }
];
