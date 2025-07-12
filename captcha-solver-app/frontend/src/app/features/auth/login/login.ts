import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service'; // Updated to auth.service.ts if renamed, or auth.ts
import { TokenStorageService } from '../token-storage.service';
import { Router } from '@angular/router'; // Removed ActivatedRoute if not used

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  form: any = {
    username: null,
    password: null
  };
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  roles: string[] = [];

  constructor(
    private authService: AuthService,
    private tokenStorage: TokenStorageService, // Injected TokenStorageService
    private router: Router
  ) { }

  ngOnInit(): void {
    if (this.tokenStorage.isLoggedIn()) {
      this.isLoggedIn = true;
      const user = this.tokenStorage.getUser();
      this.roles = user.roles || []; // Assuming roles are part of the user object stored
      // Optionally redirect if already logged in, e.g., to a dashboard
      // this.router.navigate(['/dashboard']); // Or any other default authenticated route
    }
  }

  onSubmit(): void {
    const { username, password } = this.form;

    this.authService.login({ username, password }).subscribe({
      next: data => {
        // AuthService's login method now handles saving token and user to TokenStorageService via tap operator
        this.isLoginFailed = false;
        this.isLoggedIn = true;
        const user = this.tokenStorage.getUser();
        this.roles = user.roles || [];

        // Redirect to a default page after login or reload
        // this.router.navigate(['/dashboard']); // Example redirect
        window.location.reload(); // Simple way to refresh app state and navigation
      },
      error: err => {
        this.errorMessage = err.error?.message || err.message || 'Login failed. Please check your credentials.';
        this.isLoginFailed = true;
        console.error(err);
      }
    });
  }
}
