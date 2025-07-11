import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth'; // Points to auth.ts which exports AuthService

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule], // Import CommonModule and FormsModule
  templateUrl: './register.html',
  styleUrl: './register.css' // Angular CLI might use styleUrl or styleUrls
})
export class RegisterComponent { // Renamed class
  form: any = {
    username: null,
    email: null,
    password: null
  };
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';

  constructor(private authService: AuthService) { }

  onSubmit(): void {
    const { username, email, password } = this.form;

    this.authService.register({ username, email, password }).subscribe({
      next: data => {
        console.log(data);
        this.isSuccessful = true;
        this.isSignUpFailed = false;
      },
      error: err => {
        this.errorMessage = err.error?.message || err.message || 'Sign up failed';
        this.isSignUpFailed = true;
        console.error(err);
      }
    });
  }
}
