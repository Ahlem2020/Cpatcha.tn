import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';
import { CaptchaDashboardComponent } from './features/captcha/captcha-dashboard/captcha-dashboard';
import { AuthGuard } from './features/auth/auth.guard';
import { HomeComponent } from './features/home/home';
import { ContactComponent } from './features/contact/contact';
import { DocumentationComponent } from './features/documentation/documentation';
import { SubscriptionComponent } from './features/subscription/subscription';
import { AdminComponent } from './features/admin/admin';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'home', component: HomeComponent },
    { path: 'product', component: HomeComponent },
    { path: 'contact', component: ContactComponent },
    { path: 'documentation', component: DocumentationComponent },
    { path: 'subscription', component: SubscriptionComponent },
    {
      path: 'dashboard',
      component: CaptchaDashboardComponent,
      canActivate: [AuthGuard]
    },
    {
      path: 'admin',
      component: AdminComponent,
      canActivate: [AuthGuard]
    },
    { path: '', redirectTo: 'home', pathMatch: 'full' },
];
