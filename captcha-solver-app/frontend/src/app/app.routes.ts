import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login';
import { RegisterComponent } from './auth/register/register';
import { CaptchaDashboardComponent } from './captcha/captcha-dashboard/captcha-dashboard'; // Import dashboard
import { AuthGuard } from './auth/auth.guard'; // Import AuthGuard

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    {
      path: 'dashboard',
      component: CaptchaDashboardComponent,
      canActivate: [AuthGuard] // Protect this route
    },
    // Example of how to lazy load the auth module if we were using it:
    // {
    //   path: 'auth',
    //   loadChildren: () => import('./auth/auth-module').then(m => m.AuthModule)
    // },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }, // Default to dashboard if logged in, AuthGuard will redirect to login if not
    // Add other routes here later
    // { path: '**', redirectTo: 'dashboard' } // Wildcard route
];
